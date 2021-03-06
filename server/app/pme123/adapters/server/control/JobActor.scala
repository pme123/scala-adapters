package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.google.inject.assistedinject.Assisted
import play.api.libs.json.JsValue
import pme123.adapters.server.control.mail.MailNotifier
import pme123.adapters.server.entity.ActorMessages.{SubscribeClient, UnSubscribeClient}
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.{AConcreteResult, AdaptersException}
import pme123.adapters.shared._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * This actor runs the Adapter Process.
  * During this process it will inform all clients with LogEntries.
  */
class JobActor @Inject()(@Assisted jobConfig: JobConfig
                         , @Assisted jobProcess: JobProcess
                        )(implicit val mat: Materializer, val ec: ExecutionContext)
  extends Actor
    with Logger {

  import JobActor._

  protected var logService: Option[LogService] = None

  protected def adapterVersion: String = pme123.adapters.version.BuildInfo.toString

  private var projectInfo: ProjectInfo = jobProcess.createInfo()

  // a flag that indicates if the process is running
  protected var isRunning = false

  // a map with all clients (Websocket-Actor) that needs the status about the process
  private val wsActors: mutable.Map[ClientConfig, ActorRef] = mutable.Map()

  private var lastResult: Option[AConcreteResult] = None

  // 1. level of abstraction
  // **************************
  def receive = {
    case SubscribeClient(clientConfig, wsActor) => subscribeClient(clientConfig, wsActor)
    case UnSubscribeClient(clientConfig) => unsubscribeClient(clientConfig)
    case si: SchedulerInfo =>
      projectInfo = projectInfo.copy(schedulerInfo = Some(si))
      sendToSubscriber(projectInfo)
    case RunJob(user, payload) =>
      doRunJob(user, payload)
    case RunJobFromScheduler(nextExecution) =>
      doRunJob("From Scheduler", None)
      nextExecution()
    case msg: AdapterMsg =>
      sendToSubscriber(msg)
    case LastResult(result, append) => checkAndFilter(result, append)
    case other =>
      warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************

  // subscribe a user with its id and its websocket-Actor
  // this is called when the websocket for a user is created
  private def subscribeClient(clientConfig: ClientConfig, wsActor: ActorRef) {
    info(s"Subscribed User: $clientConfig: $wsActor")
    val aRef = wsActors.getOrElseUpdate(clientConfig, wsActor)
    val status =
      if (isRunning)
        AdapterRunning(logService.get.logReport)
      else
        AdapterNotRunning(logService.map(_.logReport))
    // inform the user about the actual status
    aRef ! status
    aRef ! ClientConfigMsg(clientConfig)
    aRef ! projectInfo
    lastResult.foreach(lr =>
      aRef ! GenericResults(lastResult.map(_.clientFiltered(clientConfig)).get))
  }

  // Unsubscribe a user(remove from the map)
  // this is called when the connection from a user websocket is closed
  private def unsubscribeClient(clientConfig: ClientConfig) = {
    info(s"Unsubscribe User: $clientConfig")
    wsActors -= clientConfig
  }

  // called if a user runs the Adapter Process (Button)
  private def doRunJob(user: String, payload: Option[JsValue]) = {
    info(s"called runAdapter: $user")
    if (isRunning) // this should not happen as the button is disabled, if running
      warn("The adapter is running already!")
    else {
      info(s"run Adapter: $sender")
      isRunning = true
      sendToSubscriber(RunStarted)
      implicit val logServ: LogService = LogService(s"Run Job: ${jobConfig.webPath}", user, Some(self))
      logService = Some(logServ)
      handleImportResult(jobProcess.runJob(user, payload))
    }
  }

  private def checkAndFilter(result: AConcreteResult, append: Boolean): Unit = {
    sender() ! ClientsChange(
      wsActors.map {
        case (clientConfig, wsActor) =>
          val newResult = filterConcreteResult(clientConfig, result)
          if (newResult.nonEmpty) {
            wsActor ! GenericResults(newResult.get, append)
            Some(clientConfig)
          } else
            None
      }.filter(_.nonEmpty)
        .map(_.get)
        .toSeq
    )
    lastResult = Some(
      lastResult.map { r => if (append) result.merge(r) else result }
        .getOrElse(result)
    )
  }

  private def filterConcreteResult(clientConfig: ClientConfig
                                   , concreteResult: AConcreteResult): Option[Seq[JsValue]] = {
    val newResult = Some(concreteResult.clientFiltered(clientConfig))
    val oldResult = lastResult.map(_.clientFiltered(clientConfig))
    if (oldResult == newResult) None else newResult
  }

  // sends an AdapterMsg to all subscribed users
  private def sendToSubscriber(adapterMsg: AdapterMsg): Unit =
    wsActors.values
      .foreach(_ ! adapterMsg)

  // helper to finish import and notify the Admin
  private def handleImportResult(result: Future[LogService]) = {
    def logAndNotify() {
      result.foreach { ls =>
        ls.stopLogging()
        isRunning = false
        sendToSubscriber(RunFinished(ls.logReport))
        writeLogReportToFile(ls)
        // send an email with the report the Admin
        MailNotifier.notifyAdmin(None, ls.logReport)
      }
    }

    result.map { logServ =>
      logService = Some(logServ)
      projectInfo = projectInfo.copy(lastExecution = Some(logServ.startDateTime))
      sendToSubscriber(projectInfo)
      logAndNotify()
    }.recover {
      case _: AdaptersException => logAndNotify()
      case t: Throwable =>
        logService.map(_.error(t, "Not handled Problem in the Import"))
        logAndNotify()
    }
  }

  private def writeLogReportToFile(ls: LogService) = {
    // write the whole report to a file (incl. DEBUG messages)
    if (processLogEnabled)
      ls.writeToFile()
    else
      info("Writing LogReport to File is disabled.")
  }

}

object JobActor {
  type ClientIdent = String

  def props(jobConfig: JobConfig, jobProcess: JobProcess)
           (implicit mat: Materializer, ec: ExecutionContext): Props =
    Props(new JobActor(jobConfig, jobProcess))

  case class RunJobFromScheduler(schedulerInfo: () => Unit)

  case class ClientConfigs(clientConfigs: Seq[ClientConfig])

  case class ClientsChange(clientConfigs: Seq[ClientConfig])

  case class LastResult(payload: AConcreteResult, append: Boolean = false)

  // used to inject the JobActors as childs of the JobActorFactory
  trait Factory {
    def apply(jobConfig: JobConfig, jobProcess: JobProcess): Actor
  }

  val list = List("vbBaselIIIData_201802_3_d.data.20180405.txt.gz"
    , "vbBaselIIIData_201802_4_d.data.20180405.txt.gz"
    , "vbBaselIIIData_201803_4_d.data.20180405.txt.gz"
    , "vbBaselIIIData_201803_5_d.data.20180405.txt.gz")

  println(
    list.map { f =>
      val s = f.split("_").toList
      (s(1), f)
    }.groupBy(f => f._1)
      .map((e: (ClientIdent, List[(ClientIdent, ClientIdent)])) => e._2.max).values
  )
}

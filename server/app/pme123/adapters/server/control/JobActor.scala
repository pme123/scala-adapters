package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.google.inject.assistedinject.Assisted
import pme123.adapters.server.control.JobActor.JobConfig
import pme123.adapters.server.control.mail.MailNotifier
import pme123.adapters.server.entity.ActorMessages.{SubscribeClient, UnSubscribeClient}
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.AdaptersException
import pme123.adapters.shared.JobConfigTempl.JobIdent
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
  private val clientActors: mutable.Map[ClientConfig, ActorRef] = mutable.Map()

  private var lastResult: Option[Seq[AConcreteResult]] = None

  // 1. level of abstraction
  // **************************
  def receive = {
    case SubscribeClient(clientConfig, wsActor) => subscribeClient(clientConfig, wsActor)
    case UnSubscribeClient(clientConfig) => unsubscribeClient(clientConfig)
    case si: SchedulerInfo =>
      projectInfo = projectInfo.copy(schedulerInfo = Some(si))
      sendToSubscriber(projectInfo)
    case RunJob(user) =>
      doRunJob(user)
    case RunJobFromScheduler(nextExecution) =>
      doRunJob("From Scheduler")
      nextExecution()
    case msg: AdapterMsg =>
      sendToSubscriber(msg)
    case LastResults(results) => checkAndFilter(results)
    case other =>
      warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************

  // subscribe a user with its id and its websocket-Actor
  // this is called when the websocket for a user is created
  private def subscribeClient(clientConfig: ClientConfig, wsActor: ActorRef) {
    info(s"Subscribed User: $clientConfig: $wsActor")
    val aRef = clientActors.getOrElseUpdate(clientConfig, wsActor)
    val status =
      if (isRunning)
        AdapterRunning(logService.get.logReport)
      else
        AdapterNotRunning(logService.map(_.logReport))
    // inform the user about the actual status
    aRef ! status
    aRef ! projectInfo
  }

  // Unsubscribe a user(remove from the map)
  // this is called when the connection from a user websocket is closed
  private def unsubscribeClient(clientConfig: ClientConfig) = {
    info(s"Unsubscribe User: $clientConfig")
    clientActors -= clientConfig
  }

  // called if a user runs the Adapter Process (Button)
  private def doRunJob(user: String) = {
    info(s"called runAdapter: $user")
    if (isRunning) // this should not happen as the button is disabled, if running
      warn("The adapter is running already!")
    else {
      info(s"run Adapter: $sender")
      isRunning = true
      sendToSubscriber(RunStarted)
      implicit val logServ: LogService = LogService(s"Run Job: ${jobConfig.asString}", user, Some(self))
      logService = Some(logServ)
      handleImportResult(jobProcess.runJob(user))
    }
  }

  private def checkAndFilter(results: Seq[AConcreteResult]): Unit =
    clientActors.foreach {
      case (clientConfig, clientActor) =>
        val newResult = filterConcreteResults(clientConfig, results)
        if (newResult.nonEmpty) {
          lastResult = Some(newResult)
          clientActor ! GenericResults(newResult.map(_.toJson))
        }
    }

  private def filterConcreteResults(clientConfig: ClientConfig
                                    , concreteResults: Seq[AConcreteResult]): Seq[AConcreteResult] = {
    val newResult = concreteResults.filter(_.filter(clientConfig))
    val oldResult = lastResult.map(_.filter(_.filter(clientConfig)))
    if (oldResult.contains(newResult)) Nil else newResult
  }

  // sends an AdapterMsg to all subscribed users
  private def sendToSubscriber(adapterMsg: AdapterMsg): Unit =
    clientActors.values
      .foreach(_ ! adapterMsg)


  // helper to finish import and notify the Admin
  private def handleImportResult(result: Future[LogService]) = {
    def logAndNotify() {
      result.foreach{ls=>
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

  case class LastResults(payload: Seq[AConcreteResult])

  case class JobConfig(jobIdent: JobIdent, jobParams: Map[String, ClientConfig.ClientProperty] = Map()) {
    def asString: String = jobIdent + jobParams.map{case (k,v) => s"$k -> $v"}.mkString("[","; ", "]")
  }

  // used to inject the JobActors as childs of the JobActorFactory
  trait Factory {
    def apply(jobConfig: JobConfig, jobProcess: JobProcess): Actor
  }
}

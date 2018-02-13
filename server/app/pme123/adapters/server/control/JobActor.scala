package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.stream.Materializer
import com.google.inject.assistedinject.Assisted
import pme123.adapters.server.control.mail.MailNotifier
import pme123.adapters.server.entity.ActorMessages.{SubscribeClient, UnSubscribeClient}
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.AdaptersException
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * This actor runs the Adapter Process.
  * During this process it will inform all clients with LogEntries.
  */
class JobActor @Inject()(@Assisted jobIdent: JobIdent
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
  private val clientActors: mutable.Map[String, ActorRef] = mutable.Map()

  // 1. level of abstraction
  // **************************
  def receive = LoggingReceive {
    case SubscribeClient(clientIdent, wsActor) => subscribeClient(clientIdent, wsActor)
    case UnSubscribeClient(clientIdent) => unsubscribeClient(clientIdent)
    case si: SchedulerInfo =>
      projectInfo = projectInfo.copy(schedulerInfo = Some(si))
      sendToSubscriber(projectInfo)
    case RunJob(user) =>
      doRunJob(user)
    case RunJobFromScheduler(nextExecution) =>
      doRunJob("From Scheduler")
      nextExecution()
    case GetClientConfigs => registeredClientConfigs()
    case msg: AdapterMsg =>
      sendToSubscriber(msg)
    case other =>
      warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************

  // subscribe a user with its id and its websocket-Actor
  // this is called when the websocket for a user is created
  private def subscribeClient(clientIdent: ClientIdent, wsActor: ActorRef) {
    info(s"Subscribed User: $clientIdent: $wsActor")
    val aRef = clientActors.getOrElseUpdate(clientIdent, wsActor)
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
  private def unsubscribeClient(clientIdent: ClientIdent) = {
    info(s"Unsubscribe User: $clientIdent")
    clientActors -= clientIdent
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
      implicit val logServ: LogService = LogService(s"Run Job: $jobIdent", user, Some(self))
      logService = Some(logServ)
      handleImportResult(jobProcess.runJob(user))
    }
  }


  protected def sendToSubscriber(logEntry: LogEntry): Unit =
    sendToSubscriber(LogEntryMsg(logEntry))

  // sends an AdapterMsg to all subscribed users
  protected def sendToSubscriber(adapterMsg: AdapterMsg): Unit =
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

  private def registeredClientConfigs() {
    info(s"registeredClientConfigs: ${}")
    val configs = clientActors.keys.map(ClientConfig(_, "-"))
    sender() ! ClientConfigs(configs.toSeq)
  }
}

object JobActor {
  type ClientIdent = String

  def props(jobIdent: JobIdent, jobProcess: JobProcess)
           (implicit mat: Materializer, ec: ExecutionContext): Props =
    Props(new JobActor(jobIdent, jobProcess))

  case class RunJobFromScheduler(schedulerInfo: () => Unit)

  case object GetClientConfigs

  case class ClientConfigs(clientConfigs: Seq[ClientConfig])

}

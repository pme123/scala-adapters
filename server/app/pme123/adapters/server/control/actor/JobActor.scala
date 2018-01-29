package pme123.adapters.server.control.actor

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import akka.stream.Materializer
import pme123.adapters.server.control.LogService
import pme123.adapters.server.control.mail.MailNotifier
import pme123.adapters.server.entity.{AdaptersContext, AdaptersException}
import pme123.adapters.shared._
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.version.BuildInfo

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * This actor runs the Adapter Process.
  * During this process it will inform all clients with LogEntries.
  */
trait JobActor
  extends Actor
    with Logger {

  import JobActor._

  implicit def mat: Materializer

  implicit def ec: ExecutionContext

  protected var logService: Option[LogService] = None

  private var adapterInfo: Option[ProjectInfo] = None

  protected def adapterVersion: String = pme123.adapters.version.BuildInfo.toString

  protected def email: String = adminMailRecipient


  // a flag that indicates if the process is running
  protected var isRunning = false

  // a map with all clients (Websocket-Actor) that needs the status about the process
  private val userActors: mutable.Map[String, ActorRef] = mutable.Map()

  def receive = LoggingReceive {
    // subscribe a user with its id and its websocket-Actor
    // this is called when the websocket for a user is created
    case SubscribeAdapter(clientIdent, wsActor) =>
      info(s"Subscribed User: $clientIdent: $wsActor")
      val aRef = userActors.getOrElseUpdate(clientIdent, wsActor)
      val status =
        if (isRunning)
          AdapterRunning(logService.get.logReport)
        else
          AdapterNotRunning(logService.map(_.logReport))
      // inform the user about the actual status
      aRef ! status
      adapterInfo.foreach(aRef ! _)
    // Unsubscribe a user(remove from the map)
    // this is called when the connection from a user websocket is closed
    case UnSubscribeAdapter(clientIdent) =>
      info(s"Unsubscribe User: $clientIdent")
      userActors -= clientIdent
    // called if a user runs the Adapter Process (Button)
    case si:SchedulerInfo =>
      adapterInfo = adapterInfo.map(_.copy(schedulerInfo = Some(si)))
      adapterInfo.foreach(sendToSubscriber)
    case RunAdapter(user) =>
      doRunAdapter(user)
    case RunAdapterFromScheduler(nextExecution) =>
      doRunAdapter("From Scheduler")
      nextExecution()
    case msg: AdapterMsg =>
      sendToSubscriber(msg)
    case other =>
      warn(s"unexpected message: $other")
  }

  private def doRunAdapter(user: String) = {
    info(s"called runAdapter: $user")
    if (isRunning) // this should not happen as the button is disabled, if running
      warn("The adapter is running already!")
    else {
      info(s"run Adapter: $sender")
      isRunning = true
      sendToSubscriber(RunStarted)
      handleImportResult(runAdapter(user))
    }
  }

  // the process fakes some long taking tasks that logs its progress
  protected def runAdapter(user: String): Future[Any]


  protected def sendToSubscriber(logEntry: LogEntry): Unit =
    sendToSubscriber(LogEntryMsg(logEntry))

  // sends an AdapterMsg to all subscribed users
  protected def sendToSubscriber(adapterMsg: AdapterMsg): Unit =
    userActors.values
      .foreach(_ ! adapterMsg)

  protected def createInfo(adapterVersion: String
                           , adapterProps: Seq[AdaptersContextProp]) {
    adapterInfo = Some(ProjectInfo(adapterVersion
      , BuildInfo.version
      , email
      , adapterProps
      , AdaptersContext.props
      , logService.map(_.startDateTime)
      , None
    ))
  }

  // helper to finish import and notify the Admin
  private def handleImportResult(result: Future[Any]) = {
    def logAndNotify() {
      logService.foreach{ls=>
        ls.stopLogging()
        isRunning = false
        sendToSubscriber(RunFinished(ls.logReport))
        writeLogReportToFile(ls)
        // send an email with the report the Admin
        MailNotifier.notifyAdmin(None, ls.logReport)
      }
    }

    result.map { _ =>
      adapterInfo =
        adapterInfo.map { ai =>
          val newAI = ai.copy(lastExecution = logService.map(_.startDateTime))
          sendToSubscriber(newAI)
          newAI
        }
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
  case class SubscribeAdapter(clientIdent: ClientIdent, wsActor: ActorRef)

  case class UnSubscribeAdapter(clientIdent: ClientIdent)

  case class RunAdapterFromScheduler(schedulerInfo: () => Unit)

}

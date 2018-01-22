package pme123.adapters.shared

import java.time.Instant

import julienrf.json.derived
import play.api.libs.json.OFormat

// trait for all messages
sealed trait AdapterMsg

object AdapterMsg extends InstantHelper {
  // marshalling and unmarshalling
  // with json.validate[AdapterMsg] or Json.parse(adapterMsg)
  // this line is enough with this library - as AdapterMsg is a sealed trait
  // be aware that if you want for example json.validate[RunAdapter] you also need a OFormat[RunAdapter]
  implicit val jsonFormat: OFormat[AdapterMsg] = derived.oformat[AdapterMsg]()
}

// a client want's to start the Adapter process
// you can trigger the next SchedulerInfo - if the RunAdapter was sent by the Scheduler
case class RunAdapter(userName: String = "Anonymous")
  extends AdapterMsg

// the server indicates that the Adapter process is already running
// logReport: the LogReport of the active run.
case class AdapterRunning(logReport: LogReport) extends AdapterMsg

// the server indicates that the Adapter process is NOT running
// logReport: the LogReport of the last run - if there is one.
case class AdapterNotRunning(logReport: Option[LogReport]) extends AdapterMsg

// each LogEntry that is created by the AdapterProcess
case class LogEntryMsg(logEntry: LogEntry) extends AdapterMsg

// sent when the Adapter Process is started
case object RunStarted extends AdapterMsg

// sent when the Adapter Process finished
case class RunFinished(logReport: LogReport) extends AdapterMsg

// as with akka-http the web-socket connection will be closed when idle for too long.
case object KeepAliveMsg extends AdapterMsg

case class AdapterInfo(adapterVersion: String
                       , commonVersion: String
                       , adminMailRecipient: String
                       , adapterProps: Seq[AdaptersContextProp]
                       , commonProps: Seq[AdaptersContextProp]
                       , lastExecution: Option[Instant] = None
                       , schedulerInfo: Option[SchedulerInfo] = None
                      ) extends AdapterMsg
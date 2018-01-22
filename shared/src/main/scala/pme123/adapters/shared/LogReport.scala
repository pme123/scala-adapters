package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.LogLevel._

import scala.collection.mutable

/**
  * <pre/>
  * This allows to add LogEntries during execution. So in the end you have an ordered Sequence of LogEntries.
  * Important Functions:
  * +    add a LogEntry
  * ++   add all LogEntries of another LogReport
  */
case class LogReport(initiator: String
                     , logEntries: mutable.ListBuffer[LogEntry] = mutable.ListBuffer.empty[LogEntry])
  extends Logger {

  def logEntriesFor(logLevel: LogLevel): Seq[LogEntry] = logEntries.filter(le => le.level == logLevel)

  def count(matchStr: String): Int = logEntries.count(_.msg.contains(matchStr))

  def find(matchStr: String): Option[LogEntry] = logEntries.find(_.msg.contains(matchStr))

  def +(logEntry: LogEntry): LogEntry = synchronized {
    logEntries += logEntry
    logEntry
  }

  def ++(logReport: LogReport): LogReport = synchronized {
    for (logEntry <- logReport.logEntries) this + logEntry
    this
  }

  def createPrint(logLevel: LogLevel): String = logEntries.foldLeft("")((result, logEntry) =>
    if (logEntry.level >= logLevel) result + logEntry.asString + "\n" else result)

  def createHtmlPrint(logLevel: LogLevel): String = logEntries.foldLeft("")((result, logEntry) =>
    if (logEntry.level >= logLevel) result + logEntry.asHtmlString + "<hr>" else result)

  def printReport() {
    info(s"Start Log Report for the Adapter (started by $initiator):")
    info(createPrint(INFO))
    info("Finished Log Report.")
  }

  def maxLevel(): LogLevel = logEntries.foldLeft(DEBUG: LogLevel)((level, entry) => if (level >= entry.level) level else entry.level)

  // Seq convenience methods
  def length: Int = logEntries.length

  def apply(index: Int): LogEntry = logEntries(index)

  def isEmpty: Boolean = logEntries.isEmpty

  def clear(): Unit = synchronized {
    logEntries.clear()
  }

}

object LogReport {
  implicit val jsonFormat: OFormat[LogReport] = derived.oformat[LogReport]()

}

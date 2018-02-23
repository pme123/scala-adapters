package pme123.adapters.server.control

import java.nio.file.{Files, Paths}
import java.time.{Instant, LocalDateTime}

import akka.actor.ActorRef
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import play.api.Logger
import pme123.adapters.shared.LogLevel._
import pme123.adapters.shared.{LogEntry, LogEntryMsg, LogLevel, LogReport}
import pme123.adapters.server.entity.AdaptersContext.settings._

import scala.concurrent.{ExecutionContext, Future}

/**
  * <pre>
  * This allows to switch between InMemory Logging and MongoDB Logging.
  * MongoDB is experimental at the moment.
  * Created by pascal.mengelt on 08.12.2015.
  */
case class LogService(name: String, initiator: String, sender: Option[ActorRef] = None)
                     (implicit mat: Materializer, ec: ExecutionContext) {

  val startDateTime: Instant = Instant.now
  val logReport = LogReport(initiator)

  def print() {
    logReport.info(logReport.createPrint(DEBUG))
  }

  def logs(): List[String] = logReport.logEntries
    .map(le => le.asString).toList

  def writeToFile(): Future[Unit] = {
    val fileName = s"$name-$startDateTime".replaceAll("\\W+", "")
    val path = Paths.get(processLogPath, s"$fileName.log")
    Logger.info(s"Write log file to $processLogPath")
    Files.createDirectories(Paths.get(processLogPath))
    Source(logs())
      .map(line => ByteString(line + "\n"))
      .runWith(FileIO.toPath(path))
      // use another logger - that it is not in the LogReport
      .map(_ => Logger.info(s"Import Report saved to $path"))
      .recover{
        case exc: Exception => Logger.error("Problem write Log File.", exc)
      }
  }

  def log(logLevel: LogLevel, msg: String, detail: Option[String] = None): LogEntry =
    log(logReport.log(LogEntry(logLevel, msg, detail)))

  def debug(msg: String, detail: Option[String] = None): LogEntry =
    log(logReport.debug(msg, detail))

  def info(msg: String, detail: Option[String] = None): LogEntry =
    log(logReport.info(msg, detail))

  def warn(msg: String, detail: Option[String] = None): LogEntry =
    log(logReport.warn(msg, detail))

  def error(msg: String, detail: Option[String] = None): LogEntry =
    log(logReport.error(msg, detail))

  def error(exc: Throwable, msg: String): LogEntry =
    log(logReport.error(exc, msg))

  def log(logEntry: LogEntry): LogEntry =
    logReport + sendToSender(logEntry)

  def startLogging(): LogEntry = {
    info(s"$name started at ${LocalDateTime.ofInstant(startDateTime, timezoneID)} by $initiator")
  }

  def stopLogging(): LogEntry = {
    info(s"$name took ${Instant.now.toEpochMilli - startDateTime.toEpochMilli} ms.")
  }

  def stopLogging(clientId: String): LogEntry = {
    info(s"$name for $clientId took ${Instant.now.toEpochMilli - startDateTime.toEpochMilli} ms.")
  }

  protected def sendToSender(logEntry: LogEntry): LogEntry = {
    sender.foreach(_ ! LogEntryMsg(logEntry))
    logEntry
  }

}

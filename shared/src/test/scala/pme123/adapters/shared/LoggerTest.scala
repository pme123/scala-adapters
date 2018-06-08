package pme123.adapters.shared

import pme123.adapters.shared.LogLevel._

import scala.util.Try

/**
  * Created by pascal.mengelt on 05.03.2015.
  *
  */
class LoggerTest extends UnitTest {
  val logMsgPrefix = "this is the official test log message"
  val logParam1 = "testArg1"
  val logParam2 = "testArg2"
  val logMsg: String = s"$logMsgPrefix $logParam1 $logParam2"

  // Logger.debug
  {
    val logEntry = debug(logMsg)
    "A logged debug" should "return a LogEntry of level debug" in {
      logEntry.level should be(DEBUG)
    }
    matchLogMsg(logEntry)
    checkGenericLog(logEntry)
  }
  // Logger.info
  {
    val logEntry = Logger.info(logMsg)
    "A logged info" should "return a LogEntry of level info" in {
      logEntry.level should be(INFO)
    }
    matchLogMsg(logEntry)
    checkGenericLog(logEntry)
  }
  // Logger.warn
  {
    val logEntry = warn(logMsg)
    "A logged warn" should "return a LogEntry of level warn" in {
      logEntry.level should be(WARN)
    }
    matchLogMsg(logEntry)
    checkGenericLog(logEntry)
  }
  // Logger.error
  {
    val logEntry = error(logMsg)
    "A logged error" should "return a LogEntry of level error" in {
      logEntry.level should be(ERROR)
    }
    matchLogMsg(logEntry)
    checkGenericLog(logEntry)
  }
  // Logger.error(Throwable)
  {
    val logEntry = error(new Exception(logMsgPrefix))
    "A logged error with Exception" should "return a LogEntry of level error" in {
      logEntry.level should be(ERROR)
    }
    it should "match the log message" in {
      logEntry.msg should be(logMsgPrefix)
    }
    checkGenericLog(logEntry)
  }
  // Logger.error(Throwable, msg)
  {
    val simpleMsg = "simple log"
    val logEntry = error(new Exception(logMsgPrefix), simpleMsg)
    "A logged error with Exception and message" should "return a LogEntry of level error" in {
      logEntry.level should be(ERROR)
    }
    it should "match the log message" in {
      logEntry.msg should be(simpleMsg)
    }

    checkGenericLog(logEntry)
  }
  // Logger.error(Throwable, msg, params)
  {
    val logEntry = error(new Exception(logMsgPrefix), logMsg)
    "A logged error with Exception, message and parameters" should "return a LogEntry of level error" in {
      logEntry.level should be(ERROR)
    }
    matchLogMsg(logEntry)
    checkGenericLog(logEntry)
  }

  "AllErrorMsgs for a AdaptersException" should "be formatted in an expected format" in {
    exceptionToString(new Exception("configException")) should startWith(
      "configException ["
    )
  }


  it should "be formatted also correct for Exceptions with causes" in {
    val msg = exceptionToString(new Exception("configException", new Exception("firstCause", new Exception("secondCause"))))
      .split("\\n")
    msg.head should startWith("configException [")
    msg.tail.head should startWith(" - Cause: firstCause [")
    msg.last should startWith(" - Cause: secondCause [")
  }
  // LogEntry.asString
  {
    val logEntry = Logger.info(logMsg)
    "A LogEntry as string" should "be a nice readable text preceeded by the LogLevel" in {
      logEntry.asString should be(s"INFO: $logMsgPrefix $logParam1 $logParam2")
    }
    it should "have no problems with UTF-8 encodings" in {
      Logger.info("12%\u00e4Tafelgetr\u00e4nke\nw").asString should be("INFO: 12%\u00e4Tafelgetr\u00e4nke\nw")
    }
  }
  // LogLevel.fromLevel
  {
    "A LogLevel DEBUG" should "be created from the String Debug" in {
      LogLevel.withNameInsensitive("Debug") should be(DEBUG)
    }
    "A LogLevel INFO" should "be created from the String Info" in {
      LogLevel.withNameInsensitive("Info") should be(INFO)
    }
    "A LogLevel WARN" should "be created from the String Warn" in {
      LogLevel.withNameInsensitive("Warn") should be(WARN)
    }
    "A LogLevel ERROR" should "be created from the String Error" in {
      LogLevel.withNameInsensitive("Error") should be(ERROR)
    }

    "An unsupported Level" should "return a Failure with an IllegalArgumentException." in {
      val badLevel = "autsch"
      val fromLevel = Try(LogLevel.withNameInsensitive(badLevel))
      assert(fromLevel.isFailure)
      fromLevel.failed.get.getMessage should be("autsch is not a member of Enum (DEBUG, INFO, WARN, ERROR)")
    }
  }
  // LogLevel >= logLevel
  {
    val correct = true
    val incorrect = false
    "A LogLevel DEBUG" should "be >= than DEBUG" in {
      DEBUG >= DEBUG should be(correct)
    }
    it should "be < than INFO" in {
      DEBUG >= INFO should be(incorrect)
    }
    it should "be < than WARN" in {
      DEBUG >= WARN should be(incorrect)
    }
    it should "be < than ERROR" in {
      DEBUG >= ERROR should be(incorrect)
    }
    "A LogLevel INFO" should "be >= than DEBUG" in {
      INFO >= DEBUG should be(correct)
    }
    "A LogLevel INFO" should "be >= than INFO" in {
      INFO >= INFO should be(correct)
    }
    it should "be < than WARN" in {
      INFO >= WARN should be(incorrect)
    }
    it should "be < than ERROR" in {
      INFO >= ERROR should be(incorrect)
    }
    "A LogLevel WARN" should "be >= than INFO" in {
      WARN >= INFO should be(correct)
    }
    it should "be >= than WARN" in {
      WARN >= WARN should be(correct)
    }
    it should "be < than ERROR" in {
      WARN >= ERROR should be(incorrect)
    }
    "A LogLevel ERROR" should "be >= than INFO" in {
      ERROR >= INFO should be(correct)
    }
    it should "be >= than WARN" in {
      ERROR >= WARN should be(correct)
    }
    it should "be >= than ERROR" in {
      ERROR >= ERROR should be(correct)
    }
  }

  private def matchLogMsg(logEntry: LogEntry): Unit =
    it should "match the log message" in {
      logEntry.msg should be(logMsg)
    }

  private def checkGenericLog(logEntry: LogEntry): Unit =
    it should "also work with the generic function" in {
      val newLogEntry = log(logEntry)
      newLogEntry should be(logEntry)
    }
}

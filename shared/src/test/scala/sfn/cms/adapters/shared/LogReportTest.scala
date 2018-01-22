package sfn.cms.adapters.shared

import pme123.adapters.shared.LogLevel._
import pme123.adapters.shared._

/**
 * Created by pascal.mengelt on 13.03.2015.
 *
 */
class LogReportTest
  extends UnitTest {
    val testMsg = "test "
    val infoMsg = s"$testMsg-ok"
    val infoEntry = LogEntry(INFO, infoMsg)
    val warnEntry = LogEntry(WARN, testMsg)
    val errorEntry = LogEntry(ERROR, testMsg)

    // new
    {
      val logReport = new LogReport("tester")
      "The created LogReport" should "be empty" in {
        assert(logReport.isEmpty)
      }
    }
    // +(logEntry)
    {
      val logReport = new LogReport("tester")
      "When adding a LogEntry a LogReport" should "have one LogEntry" in {
        logReport + infoEntry
        logReport.length should be(1)
      }
      it should "match the LogEntry" in {
        logReport(0) should be(infoEntry)
      }
    }
    {
      val logReport = new LogReport("tester")
      "When adding more LogEntries, the LogReport" should "keep the order" in {
        logReport + infoEntry
        logReport + warnEntry
        logReport + errorEntry
        logReport.length should be(3)
        logReport(0) should be(infoEntry)
        logReport(1) should be(warnEntry)
        logReport(2) should be(errorEntry)
      }
    }
    // ++(logeport)
    {
      val logReport = new LogReport("tester")
      val otherLogReport = new LogReport("tester")
      "When adding an empty LogReport to the empty LogReport, it" should "have no LogEntry" in {
        logReport ++ otherLogReport
        assert(logReport.isEmpty)
      }
      "When adding a LogReport to the LogReport, it" should "have the LogEntries of both" in {
        logReport + infoEntry
        otherLogReport + infoEntry
        logReport ++ otherLogReport
        logReport.length should be(2)
      }
    }
    {
      val logReport = new LogReport("tester")
      val otherLogReport = new LogReport("tester")
      "When adding a LogReport with more LogEntries, the LogReport" should "keep the order" in {
        logReport + infoEntry
        otherLogReport + warnEntry
        otherLogReport + errorEntry
        logReport ++ otherLogReport
        logReport.length should be(3)
        logReport(0) should be(infoEntry)
        logReport(1) should be(warnEntry)
        logReport(2) should be(errorEntry)
      }
    }
    // createPrint()
    {
      val logReport = new LogReport("tester")
      "An empty LogReport" should "create an empty print" in {
        val print = logReport createPrint INFO
        assert(print.isEmpty)
      }
    }
    {
      val logReport = new LogReport("tester")
      "A LogReport with one LogEntry" should "create one line" in {
        logReport + infoEntry
        val print = logReport createPrint INFO
        print should be(infoEntry.asString + "\n")
      }
    }
    {
      val logReport = new LogReport("tester")
      logReport + infoEntry
      logReport + warnEntry
      "A LogReport with two LogEntry" should "create one line separated with '\\n'" in {
        val print = logReport createPrint INFO
        print should be(infoEntry.asString + "\n" + warnEntry.asString + "\n")
      }
      it should "create only the INFO LogEntry with the LogLevel WARN" in {
        val print = logReport createPrint WARN
        print should be(warnEntry.asString + "\n")
      }
      it should "create nothing with the LogLevel ERROR" in {
        val print = logReport createPrint ERROR
        print should be("")
      }
    }

    "A LogReport" should "print the LogReport without problems" in {
      val logReport = new LogReport("tester")
      logReport + infoEntry
      logReport + warnEntry
      val print: Unit = logReport printReport()
      assert(print.isInstanceOf[Unit])
      // nothing more to test
    }

    "After call clear, the LogReport" should "have no LogEntries" in {
      val logReport = new LogReport("tester")
      logReport + infoEntry
      logReport + warnEntry
      logReport clear()
      logReport.length should be(0)
    }

    "Calling the maxLevel of the LogReport" should "return the maximal LogEntry of that report" in {
      val logReport = new LogReport("tester")
      logReport.maxLevel() should be(DEBUG)
      logReport + infoEntry
      logReport.maxLevel() should be(INFO)
      logReport + warnEntry
      logReport.maxLevel() should be(WARN)
      logReport + errorEntry
      logReport.maxLevel() should be(ERROR)
      logReport + warnEntry
      logReport.maxLevel() should be(ERROR)
    }
    // logEntriesFor(logLevel)
    {
      "An empty LogReport" should "create an empty logEntries Seq" in {
        val logReport = new LogReport("tester")
        val logEntries = logReport logEntriesFor INFO
        assert(logEntries.isEmpty)
      }
      " A Log Report" should "return the correct logEntries INFO Seq" in {
        val logReport = new LogReport("tester")
        logReport + infoEntry
        logReport + warnEntry
        logReport + errorEntry
        val logEntries = logReport logEntriesFor INFO
        assert(logEntries.size === 1)
        assert(logEntries.head.level === INFO)
      }
    }
    // count(matchingString)
    {
      val matchStr = "Special:"
      "An empty LogReport" should "count 0" in {
        val logReport = new LogReport("tester")
        val logCount = logReport count matchStr
        assert(logCount === 0)
      }
      " A Log Report" should "return the correct count" in {
        val logReport = new LogReport("tester")
        logReport + LogEntry(WARN, matchStr + " warn")
        logReport + LogEntry(WARN, "special: warn")
        logReport + LogEntry(WARN, ">> " + matchStr + " warn")
        val logCount = logReport count matchStr
        assert(logCount == 2)
      }
    }

    // find(matchingString)
    {
      val matchStr = "Special:"
      "An empty LogReport" should "not find" in {
        val logReport = new LogReport("tester")
        val logEntry = logReport find matchStr
        assert(logEntry === None)
      }
      " A Log Report" should "return the correct (first) LogEntry that contains the search String" in {
        val logReport = new LogReport("tester")
        val entry = LogEntry(WARN, matchStr + " warn")
        logReport + entry
        logReport + LogEntry(WARN, "special: warn")
        logReport + LogEntry(WARN, ">> " + matchStr + " warn")
        val logEntry = logReport find matchStr
        assert(logEntry === Some(entry))
      }
    }
  }

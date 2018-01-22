package pme123.adapters.client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared._

trait UIStore extends Logger {
  protected def uiState: UIState

  protected def clearLogData() {
    info("UIStore: clearLogData")
    uiState.logData.value.clear()
  }

  protected def addLogReport(logReport: LogReport) {
    info(s"UIStore: addLogReport")
    uiState.logData.value ++= logReport.logEntries
  }

  protected def addLogEntry(logEntry: LogEntry) {
    info(s"UIStore: addLogEntry ${logEntry.level}: ${logEntry.msg}")
    uiState.logData.value += logEntry
  }

  protected def changeFilterText(text: String) {
    info(s"UIStore: changeFilterText $text")
    uiState.filterText.value = text
  }

  protected def changeFilterLevel(logLevel: LogLevel) {
    info(s"UIStore: changeFilterLevel $logLevel")
    uiState.filterLevel.value = logLevel
  }

  protected def changeIsRunning(running: Boolean) {
    info(s"UIStore: changeIsRunning $running")
    uiState.isRunning.value = running
  }

  protected def changeLastLogLevel(report: LogReport) {
    info(s"UIStore: changeLastLogLevel ${report.maxLevel()}")
    uiState.lastLogLevel.value = Some(report.maxLevel())
  }

  protected def changeLogEntryDetail(detail: Option[LogEntry] = None) {
    info(s"UIStore: changeLogEntryDetail ${detail.map(_.msg)}")
    uiState.logEntryDetail.value = detail
    hideAdapterInfo()
  }

  protected def changeAdapterInfo(adapterInfo: AdapterInfo) {
    info(s"UIStore: change AdapterInfo")
    uiState.adapterInfo.value = Some(adapterInfo)
  }

  protected def showAdapterInfo() {
    info(s"UIStore: show AdapterInfo")
    uiState.showAdapterInfo.value = true
    uiState.logEntryDetail.value = None
  }

  protected def hideAdapterInfo() {
    info(s"UIStore: hide AdapterInfo")
    uiState.showAdapterInfo.value = false
  }

  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???

}

case class UIState(logData: Vars[LogEntry] = Vars[LogEntry]()
                   , isRunning: Var[Boolean] = Var(false)
                   , filterText: Var[String] = Var("")
                   , filterLevel: Var[LogLevel] = Var[LogLevel](LogLevel.INFO)
                   , lastLogLevel: Var[Option[LogLevel]] = Var[Option[LogLevel]](None)
                   , logEntryDetail: Var[Option[LogEntry]] = Var[Option[LogEntry]](None)
                   , adapterInfo: Var[Option[AdapterInfo]] = Var[Option[AdapterInfo]](None)
                   , showAdapterInfo: Var[Boolean] = Var(false)
                  )

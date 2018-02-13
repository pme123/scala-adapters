package pme123.adapters.client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.JsValue
import pme123.adapters.shared._

import scala.language.implicitConversions

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
    hideAllDialogs()
    uiState.logEntryDetail.value = detail
  }

  protected def changeAdapterInfo(adapterInfo: ProjectInfo) {
    info(s"UIStore: change AdapterInfo")
    uiState.adapterInfo.value = Some(adapterInfo)
  }

  protected def showAdapterInfo() {
    info(s"UIStore: show AdapterInfo")
    hideAllDialogs()
    uiState.showAdapterInfo.value = true
  }

  protected def showClients() {
    info(s"UIStore: showClients")
    hideAllDialogs()
    uiState.showClients.value = true
  }

  protected def hideAdapterInfo() {
    info(s"UIStore: hide AdapterInfo")
    uiState.showAdapterInfo.value = false
  }

  protected def changeJobConfigs(jobConfigs: JobConfigs): Unit = {
    info(s"UIStore: changeJobConfigs ${jobConfigs.configs.keys.mkString(", ")}")
    uiState.jobConfigs.value = jobConfigs
  }

  protected def changeSelectedJobConfig(jobConfig: Option[JobConfig]): Unit = {
    info(s"UIStore: changeSelectedJobConfig ${jobConfig.map(_.ident)}")
    uiState.selectedJobConfig.value = jobConfig
  }

  protected def replaceLastResults(lastResults: Seq[JsValue]) {
    info(s"UIStore: replaceLastResults")
    uiState.lastResults.value.clear()
    uiState.lastResults.value ++= lastResults
  }

  protected def addLastResult(lastResult: JsValue) {
    info(s"UIStore: addLastResult: $lastResult")
    uiState.lastResults.value += lastResult
  }

  protected def replaceAllClients(clientConfigs: Seq[ClientConfig]) {
    info(s"UIStore: replaceAllClients")
    uiState.allClients.value.clear()
    uiState.allClients.value ++= clientConfigs
  }

  // make sure all are closed
  private def hideAllDialogs(): Unit = {
    uiState.showClients.value = false
    uiState.showAdapterInfo.value = false
    uiState.logEntryDetail.value = None
  }

  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???

}

case class UIState(logData: Vars[LogEntry] = Vars[LogEntry]()
                   , isRunning: Var[Boolean] = Var(false)
                   , filterText: Var[String] = Var("")
                   , filterLevel: Var[LogLevel] = Var[LogLevel](LogLevel.INFO)
                   , lastLogLevel: Var[Option[LogLevel]] = Var[Option[LogLevel]](None)
                   , logEntryDetail: Var[Option[LogEntry]] = Var[Option[LogEntry]](None)
                   , adapterInfo: Var[Option[ProjectInfo]] = Var[Option[ProjectInfo]](None)
                   , showAdapterInfo: Var[Boolean] = Var(false)
                   , showClients: Var[Boolean] = Var(false)
                   , jobConfigs: Var[JobConfigs] = Var(JobConfigs(Map()))
                   , selectedJobConfig: Var[Option[JobConfig]] = Var(None)
                   , lastResults: Vars[JsValue] = Vars()
                   , allClients: Vars[ClientConfig] = Vars()

                  )

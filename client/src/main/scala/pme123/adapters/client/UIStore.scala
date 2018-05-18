package pme123.adapters.client

import com.thoughtworks.binding.Binding.{Var, Vars}
import play.api.libs.json._
import pme123.adapters.shared._

import scala.language.implicitConversions
import scala.reflect.ClassTag

object UIStore extends Logger {
  val maxLogEntries = 200
  val uiState = UIState()

  def clearLogData() {
    info("UIStore: clearLogData")
    uiState.logData.value.clear()
  }

  def addLogReport(logReport: LogReport) {
    info(s"UIStore: addLogReport")
    uiState.logData.value ++= logReport.logEntries
    restrictEntries()
  }

  def addLogEntry(logEntry: LogEntry) {
    info(s"UIStore: addLogEntry ${logEntry.level}: ${logEntry.msg}")
    uiState.logData.value += logEntry
    restrictEntries()
  }

  private def restrictEntries() {
    if (uiState.logData.value.length > maxLogEntries)
      uiState.logData.value.remove(0, uiState.logData.value.length - maxLogEntries)
  }

  def changeFilterText(text: String) {
    info(s"UIStore: changeFilterText $text")
    uiState.filterText.value = text
  }

  def changeFilterLevel(logLevel: LogLevel) {
    info(s"UIStore: changeFilterLevel $logLevel")
    uiState.filterLevel.value = logLevel
  }

  def changeIsRunning(running: Boolean) {
    info(s"UIStore: changeIsRunning $running")
    uiState.isRunning.value = running
  }

  def changeLastLogLevel(report: LogReport) {
    info(s"UIStore: changeLastLogLevel ${report.maxLevel()}")
    uiState.lastLogLevel.value = Some(report.maxLevel())
  }

  def changeLogEntryDetail(detail: Option[LogEntry] = None) {
    info(s"UIStore: changeLogEntryDetail ${detail.map(_.msg)}")
    hideAllDialogs()
    uiState.logEntryDetail.value = detail
  }

  def changeProjectInfo(adapterInfo: ProjectInfo) {
    info(s"UIStore: change ProjectInfo")
    uiState.adapterInfo.value = Some(adapterInfo)
  }

  def showProjectInfo() {
    info(s"UIStore: show ProjectInfo")
    hideAllDialogs()
    uiState.showProjectInfo.value = true
  }

  def showJobs() {
    info(s"UIStore: showJobs")
    hideAllDialogs()
    uiState.showJobs.value = true
  }

  def showClients() {
    info(s"UIStore: showClients")
    hideAllDialogs()
    uiState.showClients.value = true
  }

  def showLastResults() {
    info(s"UIStore: showLastResults")
    hideAllDialogs()
    uiState.showLastResults.value = true
  }


  def showRunJobDialog() {
    info(s"UIStore: showRunJobDialog")
    hideAllDialogs()
    uiState.showRunJobDialog.value = true
  }

  def hideProjectInfo() {
    info(s"UIStore: hide ProjectInfo")
    uiState.showProjectInfo.value = false
  }

  def changeJobConfigs(jobConfigs: Seq[JobConfig]): Unit = {
    info(s"UIStore: changeJobConfigs ${jobConfigs.map(_.jobIdent).mkString(", ")}")
    uiState.allJobs.value.clear()
    uiState.allJobs.value ++= jobConfigs
  }

  def changeSelectedClientConfig(clientConfig: Option[ClientConfig]): Unit = {
    info(s"UIStore: changeSelectedClientConfig ${clientConfig.map(_.jobConfig.jobIdent)}")
    uiState.selectedClientConfig.value = clientConfig
  }

  def replaceLastResults(lastResults: Seq[JsValue], append: Boolean) {
    info(s"UIStore: replaceLastResults")
    if (!append)
      uiState.lastResults.value.clear()
    uiState.lastResults.value ++= lastResults
  }

  def replaceLastResult(lastResult: JsValue, append: Boolean) {
    info(s"UIStore: addLastResult: $lastResult")
    if (!append)
      uiState.lastResults.value.clear()
    uiState.lastResults.value += lastResult
  }

  def replaceAllClients(clientConfigs: Seq[ClientConfig]) {
    info(s"UIStore: replaceAllClients")
    uiState.allClients.value.clear()
    uiState.allClients.value ++= clientConfigs
  }

  def changeResultCount(countStr: String) {
    info(s"UIStore: changeResultCount $countStr")
    val clientConfig = uiState.selectedClientConfig.value
    uiState.selectedClientConfig.value =
      clientConfig
        .map(_.copy(resultCount =
          if (countStr.trim.isEmpty) ClientConfig.defaultResultCount else countStr.toInt))
  }

  def changeResultFilter(filterStr: String) {
    info(s"UIStore: changeResultFilter $filterStr")
    val newFilter = if (filterStr.trim.isEmpty) None else Some(filterStr)
    val clientConfig = uiState.selectedClientConfig.value
    uiState.selectedClientConfig.value =
      clientConfig
        .map(_.copy(resultFilter = newFilter))
  }

  // make sure all are closed
  private def hideAllDialogs(): Unit = {
    uiState.showJobs.value = false
    uiState.showClients.value = false
    uiState.showLastResults.value = false
    uiState.showProjectInfo.value = false
    uiState.showRunJobDialog.value = false
    uiState.logEntryDetail.value = None
  }
}

case class UIState(logData: Vars[LogEntry] = Vars.empty[LogEntry]
                   , isRunning: Var[Boolean] = Var(false)
                   , filterText: Var[String] = Var("")
                   , filterLevel: Var[LogLevel] = Var[LogLevel](LogLevel.INFO)
                   , lastLogLevel: Var[Option[LogLevel]] = Var[Option[LogLevel]](None)
                   , logEntryDetail: Var[Option[LogEntry]] = Var[Option[LogEntry]](None)
                   , adapterInfo: Var[Option[ProjectInfo]] = Var[Option[ProjectInfo]](None)
                   , showProjectInfo: Var[Boolean] = Var(false)
                   , showJobs: Var[Boolean] = Var(false)
                   , showClients: Var[Boolean] = Var(false)
                   , showLastResults: Var[Boolean] = Var(false)
                   , showRunJobDialog: Var[Boolean] = Var(false)
                   , allJobs: Vars[JobConfig] = Vars.empty[JobConfig]
                   , selectedClientConfig: Var[Option[ClientConfig]] = Var(None)
                   , lastResults: Vars[JsValue] = Vars.empty[JsValue]
                   , allClients: Vars[ClientConfig] = Vars.empty[ClientConfig]
                   , jobResultsRows: Vars[JobResultsRow] = Vars.empty[JobResultsRow]
                  )

object ToConcreteResults
  extends Logger {

  // type class
  trait ConcreteResult[A] {
    def fromJson(jsValue: JsValue): JsResult[A]
  }

  def toConcreteResults[A: ConcreteResult : ClassTag](concreteResults: Vars[A]
                                                      , lastResults: Seq[JsValue])
                                                     (implicit aConcreteResult: ConcreteResult[A]): Seq[A] = {

    // use the ConcreteResult.fromJson to get the concrete result entity
    def toConcreteResult(lastResult: JsValue): List[A] = {
      val clazz = implicitly[ClassTag[A]].runtimeClass
      aConcreteResult.fromJson(lastResult) match {
        case JsSuccess(cResult: A, _) if clazz.isInstance(cResult) =>
          List(cResult)
        case JsError(errors) =>
          error(s"Problem parsing ConcreteResult: ${errors.map(e => s"${e._1} -> ${e._2}")}")
          Nil
      }
    }

    if (lastResults.isEmpty) {
      concreteResults.value.clear()
    }
    else if (lastResults.size - concreteResults.value.size == 1) {
      val cRes = toConcreteResult(lastResults.last)
      info(s"added another ConcreteResult: $cRes")
      concreteResults.value ++= cRes
    }
    else {
      info("update all last results")
      concreteResults.value ++= lastResults.flatMap(lr => toConcreteResult(lr))
    }
    concreteResults.value
  }
}



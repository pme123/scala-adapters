package pme123.adapters.client

import com.thoughtworks.binding.Binding.{Var, Vars}
import play.api.libs.json._
import pme123.adapters.shared._

import scala.language.implicitConversions
import scala.reflect.ClassTag

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

  protected def showLastResults() {
    info(s"UIStore: showLastResults")
    hideAllDialogs()
    uiState.showLastResults.value = true
  }

  protected def hideAdapterInfo() {
    info(s"UIStore: hide AdapterInfo")
    uiState.showAdapterInfo.value = false
  }

  protected def changeJobConfigs(jobConfigs: JobConfigs): Unit = {
    println(s"UIStore: changeJobConfigs ${jobConfigs.configs.map(_.jobIdent).mkString(", ")}")
    uiState.jobConfigs.value = jobConfigs
  }

  protected def changeSelectedClientConfig(clientConfig: Option[ClientConfig]): Unit = {
    println(s"UIStore: changeSelectedClientConfig ${clientConfig.map(_.jobIdent)}")
    uiState.selectedClientConfig.value = clientConfig
  }

  protected def replaceLastResults(lastResults: Seq[JsValue], append: Boolean) {
    info(s"UIStore: replaceLastResults")
    if (!append)
      uiState.lastResults.value.clear()
    uiState.lastResults.value ++= lastResults
  }

  protected def replaceLastResult(lastResult: JsValue, append: Boolean) {
    info(s"UIStore: addLastResult: $lastResult")
    if (!append)
      uiState.lastResults.value.clear()
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
    uiState.showLastResults.value = false
    uiState.showAdapterInfo.value = false
    uiState.logEntryDetail.value = None
  }
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
                   , showLastResults: Var[Boolean] = Var(false)
                   , jobConfigs: Var[JobConfigs] = Var(JobConfigs())
                   , selectedClientConfig: Var[Option[ClientConfig]] = Var(None)
                   , lastResults: Vars[JsValue] = Vars()
                   , allClients: Vars[ClientConfig] = Vars()
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
          error(s"Problem parsing DemoResult: ${errors.map(e => s"${e._1} -> ${e._2}")}")
          Nil
      }
    }

    if (lastResults.isEmpty) {
      concreteResults.value.clear()
    }
    else if (lastResults.size - concreteResults.value.size == 1) {
      val iElem = toConcreteResult(lastResults.last)
      info(s"added another ImageElem: $iElem")
      concreteResults.value ++= iElem
    }
    else {
      info("update all last results")
      concreteResults.value ++= lastResults.flatMap(lr => toConcreteResult(lr))
    }
    concreteResults.value
  }
}



package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw._
import org.scalajs.jquery.jQuery
import pme123.adapters.shared.LogEntry
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.timers.setTimeout

case class JobCockpitClient(context: String)
  extends UIStore
    with ClientUtils {

  LoggerConfig.factory = ConsoleLoggerFactory()

  val uiState = UIState()


  def create(): Unit = {
    dom.render(document.body, render)
    import SemanticUI.jq2semantic
    jQuery(".ui.dropdown").dropdown(js.Dynamic.literal(on = "hover"))
  }

  @dom
  private def render: Binding[HTMLElement] = {
    <div>
      {JobCockpitHeader(context, uiState).showHeader().bind}{//
      ServerServices(uiState).jobConfigs(context).bind}{//
      adapterContainer.bind}{//
      renderDetail.bind}{//
      renderLogEntryDetail.bind}
    </div>
  }


  @dom
  private def adapterContainer = {
    val logEntries = uiState.logData.bind
    val text = uiState.filterText.bind
    val level = uiState.filterLevel.bind
    val filteredLE =
      logEntries
        .filter(le => le.level >= level)
        .filter(le => le.msg.toLowerCase.contains(text.toLowerCase))

    <div class="ui main text container">
      <div id="log-panel" class="ui relaxed divided list">
        {Constants(filteredLE: _*).map(logEntry(_).bind)}
      </div>
    </div>
  }

  @dom
  private def logEntry(entry: LogEntry) =
    <div class="item">
      {Constants(entry.detail.toList: _*).map(_ => logEntryDetail(entry).bind)}{//
      logLevelIcon(entry).bind}<div class="content">
      <div class="header">
        {entry.msg}
      </div>
      <div class="description">
        {jsLocalDateTime(entry.timestamp)}
      </div>
    </div>
    </div>


  @dom
  private def logEntryDetail(detail: LogEntry) =
    <div class="right floated content">
      <button class="ui basic icon button"
              onclick={_: Event =>
                changeLogEntryDetail(Some(detail))
                setTimeout(200) {
                  import SemanticUI.jq2semantic
                  jQuery(".ui.modal").modal("show")
                }}
              data:data-tooltip="Show details of the Log Entry"
              data:data-position="bottom right">
        <i class="expand icon large"></i>
      </button>
    </div>

  @dom
  private def renderDetail = {
    val show = uiState.showAdapterInfo.bind
    val adapterInfo = uiState.adapterInfo.bind
    if (show)
      <div>
        {Constants(adapterInfo.toSeq: _*).map(ai => ProjectInfoDialog(ai, uiState).showDetail().bind)}
      </div>
    else
      <div></div>
  }

  @dom
  private def renderLogEntryDetail = {
    val leDetail = uiState.logEntryDetail.bind

    if (leDetail.isDefined)
      <div>
        {Constants(leDetail.toSeq: _*).map(d => LogEntryDetailDialog(d, uiState).showDetail().bind)}
      </div>
    else
      <div></div>
  }

}

object JobCockpitClient {

  @JSExportTopLevel("client.JobCockpitClient.main")
  def main(context: String): Unit = {
    println(s"JobCockpitClient $context")
    JobCockpitClient(context).create()
  }
}
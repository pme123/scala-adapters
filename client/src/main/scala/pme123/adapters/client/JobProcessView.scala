package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw._
import org.scalajs.jquery.jQuery
import pme123.adapters.shared.LogEntry

import scala.language.implicitConversions
import scala.scalajs.js.timers.setTimeout

case class JobProcessView(socket: ClientWebsocket
                          , context: String
                          , websocketPath: String
                          , runJobDialog: RunJobDialog)
  extends AdaptersClient {

  @dom
  protected def render: Binding[HTMLElement] = {
    socket.connectWS(Some(websocketPath))
    <div>
      {JobProcessHeader(socket).showHeader().bind}{//
      adapterContainer.bind}{//
      renderDetail.bind}{//
      renderLogEntryDetail.bind}{//
      renderJobConfigsDetail.bind}{//
      renderClientConfigsDetail.bind}{//
      renderLastResultsDetail.bind}{//
      runJobDialog.create().bind}
    </div>
  }


  @dom
  private lazy val adapterContainer = {
    val text = UIStore.uiState.filterText.bind
    val level = UIStore.uiState.filterLevel.bind
    <div id="log-container" class="ui main container">
      <div id="log-panel" class="ui relaxed divided list">
        {for {
        le: LogEntry <- UIStore.uiState.logData
        if le.level >= level
        if le.msg.toLowerCase.contains(text.toLowerCase)
      } yield logEntry(le).bind}
      </div>
    </div>
  }

  @dom
  private def logEntry(entry: LogEntry) =
    <div class="item">
      {//
      scrollDown()
      Constants(entry.detail.toList: _*).map(_ => logEntryDetail(entry).bind)}{//
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
                UIStore.changeLogEntryDetail(Some(detail))
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
    val show = UIStore.uiState.showProjectInfo.bind
    val adapterInfo = UIStore.uiState.adapterInfo.bind
    if (show)
      <div>
        {Constants(adapterInfo.toSeq: _*).map(ai => ProjectInfoDialog(ai).showDetail().bind)}
      </div>
    else
      <div></div>
  }

  @dom
  private def renderLogEntryDetail = {
    val leDetail = UIStore.uiState.logEntryDetail.bind

    if (leDetail.isDefined)
      <div>
        {Constants(leDetail.toSeq: _*).map(d => LogEntryDetailDialog(d).showDetail().bind)}
      </div>
    else
      <div></div>
  }

  @dom
  private def renderJobConfigsDetail = {
    val showJobs = UIStore.uiState.showJobs.bind
    if (showJobs)
      <div>
        {JobConfigDialog(context)
        .showDetail().bind}
      </div>
    else
      <div></div>
  }

  @dom
  private def renderClientConfigsDetail = {
    val showClients = UIStore.uiState.showClients.bind
    val selectedClientConfig = UIStore.uiState.selectedClientConfig.bind
    if (showClients && selectedClientConfig.isDefined)
      <div>
        {ClientConfigDialog(context)
        .showDetail().bind}
      </div>
    else
      <div></div>
  }

  @dom
  private def renderLastResultsDetail = {
    val showLastResults = UIStore.uiState.showLastResults.bind
    val selectedJobConfig = UIStore.uiState.selectedClientConfig.bind
    if (showLastResults && selectedJobConfig.isDefined)
      <div>
        {LastResultDialog()
        .showDetail().bind}
      </div>
    else
      <div></div>
  }

}
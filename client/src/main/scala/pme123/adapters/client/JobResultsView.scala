package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw._
import org.scalajs.jquery.jQuery
import play.api.libs.json.JsValue
import pme123.adapters.client.SemanticUI.jq2semantic
import pme123.adapters.client.ToConcreteResults.{ConcreteResult, toConcreteResults}
import pme123.adapters.shared.ClientConfig
import pme123.adapters.shared.ClientConfig.{resultCountL, resultFilterL}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.URIUtils

case class JobResultsView(context: String
                          , websocketPath: String
                          , resultsInfos: CustomResultsInfos)
                         (implicit concreteResult: ConcreteResult[JobResultsRow])
  extends AdaptersClient
    with ClientUtils {

  lazy val socket: ClientWebsocket = ClientWebsocket(context)

  // 1. level of abstraction
  // **************************

  override def create(): Unit = {
    dom.render(document.body, render)
    jQuery(".ui.dropdown").dropdown(js.Dynamic.literal(on = "hover"))
    jQuery(".ui.item .ui.input").popup(js.Dynamic.literal(on = "hover"))
  }

  @dom
  def render: Binding[HTMLElement] = {
    socket.connectWS(Some(websocketPath))
    <div>
      {adapterHeader.bind}{//
      resultsTable.bind}
    </div>
  }

  // 2. level of abstraction
  // **************************

  @dom
  private def adapterHeader = {
    <div class="ui main fixed borderless menu">
      {faviconElem.bind}{//
      headerTitle.bind}
      <div class="right menu">
        {resultCountField.bind}{//
        filterField.bind}{//
        reconnectWSButton.bind}
      </div>
    </div>
  }

  @dom
  private def resultsTable = {
    val lastResults = UIStore.uiState.lastResults.bind
    toConcreteResults(UIStore.uiState.jobResultsRows, lastResults)
    <div class="ui main container">
        <table class="ui padded table">
        <thead>
          <tr>
            {Constants(resultsInfos.headerColumns.map(headerCell): _*)
            .map(_.bind)}
          </tr>
        </thead>
        <tbody>
          {Constants(UIStore.uiState.jobResultsRows.value.map(_.resultRow): _*)
          .map(_.bind)}
        </tbody>
      </table>
    </div>
  }

  // 3. level of abstraction
  // **************************

  @dom
  private def headerTitle = {
    val clientConfig = UIStore.uiState.selectedClientConfig.bind
    <div class="ui header item">
      {s"Last Result (as raw JSON): ${clientConfig.map(_.jobConfig.webPath).getOrElse("")}"}
    </div>
  }

  @dom
  private def resultCountField = {
    <div class="ui item">
      <div class="ui input resultCount"
           data:data-tooltip="Number of results"
           data:data-position="bottom right">
        <input id="resultCountInput"
               type="number"
               placeholder="Results..."
               size={3}
               onchange={_: Event =>
                 UIStore.changeResultCount(s"${resultCountInput.value}")}>
        </input>
      </div>
    </div>
  }

  @dom
  private def filterField = {
    <div class="ui item">
      <div class="ui input" data:data-html={resultsInfos.filterTooltip}
           data:data-position="bottom right"
           data:data-variation="very wide">
        <input id="filterInput"
               type="text"
               size={45}
               placeholder="Filter Results..."
               onkeyup={_: Event =>
                 UIStore.changeResultFilter(s"${filterInput.value}")}>
        </input>
      </div>
    </div>
  }

  @dom
  private def reconnectWSButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event =>
                connectToWebsocket(UIStore.uiState.selectedClientConfig.value)}
              data:data-tooltip="Reconnect the WebSocket (new filter)"
              data:data-position="bottom right">
        <i class="refresh icon large"></i>
      </button>
    </div>
  }

  @dom
  private def resultRow(result: JsValue) =
    <tr>
      <td class="sixteen wide">
        {result.toString}
      </td>
    </tr>

  private def connectToWebsocket(clientConfig: Option[ClientConfig]) {
    clientConfig.map { clientConfig =>
      val resultCount = s"$resultCountL=${clientConfig.resultCount}"
      val filter = clientConfig.resultFilter.map(f => s"$resultFilterL=${URIUtils.encodeURIComponent(f)}").getOrElse("")
      s"${clientConfig.jobConfig.webPath}?$resultCount&$filter"
    }.foreach(path => socket.connectWS(Some(path)))
  }

  @dom
  private def headerCell(name: String) =
    <th>
      {name}
    </th>

}

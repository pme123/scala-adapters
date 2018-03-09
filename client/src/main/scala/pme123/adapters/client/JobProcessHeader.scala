package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, HTMLElement}
import org.scalajs.jquery.jQuery
import pme123.adapters.client.SemanticUI.jq2semantic
import pme123.adapters.shared.LogLevel

import scala.language.implicitConversions
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.timers.setTimeout

private[client] case class JobProcessHeader(context: String
                                            , websocketPath: String
                                            , uiState: UIState
                                            , socket: ClientWebsocket)
  extends UIStore
    with ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showHeader(): Binding[HTMLElement] = {
    <div class="ui main fixed borderless menu">
      <div class="ui item">
        <img src={"" + g.jsRoutes.controllers.Assets.versioned("images/favicon.png").url}></img>
      </div>{title.bind //
      }<div class="right menu">
      {lastLevel.bind}{//
      textFilter.bind}{//
      levelFilter.bind}{//
      runAdapterButton.bind}{//
      jobsButton.bind}{//
      clientsButton.bind}{//
      jsonButton.bind}{//
      clearButton.bind}{//
      infoButton.bind}
    </div>
    </div>
  }

  // 2. level of abstraction
  // **************************

  @dom
  private def title = {
    val clientConfig = uiState.selectedClientConfig.bind
    <div class="ui header item">
      {s"Job Cockpit: ${clientConfig.map(_.jobConfig.jobIdent).getOrElse("")}"}
    </div>
  }
  @dom
  private def lastLevel = {
    val logLevel = uiState.lastLogLevel.bind

    @dom
    def logImage(levelClass: String): Binding[HTMLElement] =
      <i class={"large middle aligned " + levelClass}></i>

    val levelClass: Option[Binding[HTMLElement]] = logLevel
      .map(SemanticUI.levelClass)
      .map(logImage)

    @dom
    def logConstants(levelClass: Option[Binding[HTMLElement]]) =
      Constants(levelClass.toList: _*)
        .map(_.bind)

    <div class="ui item">
      <span
           data:data-tooltip={"Log level last Adapter Process: " + logLevel.getOrElse("Not run!")}
           data:data-position="bottom center">
        {logConstants(levelClass).bind}
      </span>
    </div>
  }

  // filterInput references to the id of the input (macro magic)
  // this creates a compile exception in intellij
  @dom
  private def textFilter = {
    <div class="ui item">
      <div class="ui input"
           data:data-tooltip="Filter by text."
           data:data-position="bottom right">
        <input id="filterInput"
               type="text"
               placeholder="Filter..."
               onkeyup={_: Event =>
                 changeFilterText(s"${filterInput.value}")}>
        </input>
      </div>
    </div>
  }

  // filterInput references to the id of the input (macro magic)
  // this creates a compile exception in intellij
  @dom
  private def levelFilter = {
    implicit def stringToBoolean(str: String): Boolean = str == "true"

    <div class="ui item">
      <span data:data-tooltip="Filter the Logs by its Level"
            data:data-position="bottom right">
        <select id="filterSelect"
                class="ui compact dropdown"
                onchange={_: Event =>
                  changeFilterLevel(LogLevel.fromLevel(s"${filterSelect.value}").get)}>
          <option value="ERROR">ERROR</option>
          <option value="WARN">WARN</option>
          <option value="INFO" selected={true}>INFO</option>
          <option value="DEBUG">DEBUG</option>
        </select>
      </span>
    </div>
  }

  @dom
  private def runAdapterButton = {
    val runDisabled = uiState.isRunning.bind

    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event => socket.runAdapter()}
              disabled={runDisabled}
              data:data-tooltip="Run the Adapter"
              data:data-position="bottom right">
        <i class="toggle right icon large"></i>
      </button>
    </div>
  }

  @dom
  private def jobsButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event =>
                showJobs()
                setTimeout(200) {
                  jQuery(".ui.modal").modal("show")
                }}
              data:data-tooltip="Show the registered Jobs"
              data:data-position="bottom right">
        <i class="cogs icon large"></i>
      </button>
    </div>
  }

  @dom
  private def clientsButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event =>
                showClients()
                setTimeout(200) {
                  jQuery(".ui.modal").modal("show")
                }}
              data:data-tooltip="Show the registered Clients"
              data:data-position="bottom right">
        <i class="list icon large"></i>
      </button>
    </div>
  }

  @dom
  private def jsonButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event =>
                showLastResults()
                setTimeout(200) {
                  jQuery(".ui.modal").modal("show")
                }}
              data:data-tooltip="Show the last Result as JSON-Objects"
              data:data-position="bottom right">
        <i class="file code outline icon large"></i>
      </button>
    </div>
  }

  @dom
  private def clearButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event => clearLogData()}
              data:data-tooltip="Clear the console"
              data:data-position="bottom right">
        <i class="remove circle outline icon large"></i>
      </button>
    </div>
  }

  @dom
  private def infoButton = {
    <div class="ui item">
      <button class="ui basic icon button"
              onclick={_: Event =>
                showProjectInfo()
                setTimeout(200) {
                  jQuery(".ui.modal").modal("show")
                }}
              data:data-tooltip="Get infos of the adapter"
              data:data-position="bottom right">
        <i class="info circle icon large"></i>
      </button>
    </div>
  }


}

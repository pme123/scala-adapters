package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, HTMLElement}
import org.scalajs.jquery.jQuery
import pme123.adapters.client.SemanticUI.jq2semantic
import pme123.adapters.client.{ClientWebsocket, RunJobDialog, UIStore}
import pme123.adapters.shared.Logger

import scala.scalajs.js.timers.setTimeout


case class DemoRunJobDialog(socket: ClientWebsocket)
  extends RunJobDialog
    with Logger {

  @dom
  def create(): Binding[HTMLElement] = {
    val show = UIStore.uiState.showRunJobDialog.bind
    if (show) {
      info("Open DemoRunJobDialog")
      <div class="ui modal">
        {//
        demoHeader.bind}{//
        demoForm.bind}
      </div>
    } else
      <div></div>
  }

  // 2. level of abstraction
  // **************************

  @dom
  private def demoHeader: Binding[HTMLElement] = <div class="header">
    Needed Demo Content
  </div>

  @dom
  private def demoForm: Binding[HTMLElement] = {
    <div class="content">
      <div class="ui form">
        <div class="field">
          <label>Description</label>
          <input type="text" name="description" placeholder="..."/>
        </div>
        <div class="field">
          <label>Last Name</label>
          <input type="text" name="last-name" placeholder="Last Name"/>
        </div>

        <button class="ui basic icon button"
                onclick={_: Event =>
                  socket.runAdapter()
                }>Submit</button>
      </div>
    </div>
  }


}

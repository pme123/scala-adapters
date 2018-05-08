package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.Logger

trait RunJobDialog {
  def create(): Binding[HTMLElement]
}

case class DefaultRunJobDialog(socket: ClientWebsocket)
  extends RunJobDialog
    with Logger {

  @dom
  def create(): Binding[HTMLElement] = {
    val show = UIStore.uiState.showRunJobDialog.bind
    if (show) {
      socket.runAdapter()
      info("Run Adapter")

    }
    <div></div>
  }

}

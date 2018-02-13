package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import slogging.{ConsoleLoggerFactory, LoggerConfig}

trait AdaptersClient extends UIStore
  with ClientUtils {

  LoggerConfig.factory = ConsoleLoggerFactory()

  val uiState = UIState()


  def create(): Unit = {
    dom.render(document.body, render)
  }

  protected def render: Binding[HTMLElement]
}

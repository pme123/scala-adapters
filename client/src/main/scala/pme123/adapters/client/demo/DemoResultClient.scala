package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw._
import pme123.adapters.client.{AdaptersClient, ClientWebsocket}
import pme123.adapters.shared.Logger
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

case class DemoResultClient(context: String, websocketPath: String)
  extends AdaptersClient
    with DemoUIStore {

  val demoUIState = DemoUIState()

  private lazy val socket = ClientWebsocket(uiState, context)

  @dom
  protected def render: Binding[HTMLElement] = {
    socket.connectWS(Some(websocketPath))
    <div>
      {imageContainer.bind}
    </div>
  }

  // 2. level of abstraction
  // **************************
  @dom
  private def imageContainer = {
    val demoResults = uiState.lastResults.bind
    <div>
      {Constants(updateImageElems(demoResults): _*)
      .map(_.imageElement.bind)}
    </div>
  }
}

object DemoResultClient extends Logger {

  LoggerConfig.factory = ConsoleLoggerFactory()

  @JSExportTopLevel("client.DemoResultClient.main")
  def main(context: String, websocketPath: String): Unit = {
    info(s"JobCockpitClient $context$websocketPath")
    DemoResultClient(context, websocketPath).create()
  }
}


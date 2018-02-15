package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw._
import pme123.adapters.client.{AdaptersClient, ClientWebsocket}
import pme123.adapters.shared.demo.DemoJobs.demoJobWithoutSchedulerIdent

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

case class DemoResultClient(context: String)
  extends AdaptersClient
    with DemoUIStore {

  val demoUIState = DemoUIState()

  private lazy val socket = ClientWebsocket(uiState, context)

  @dom
  protected def render: Binding[HTMLElement] = {
    socket.connectWS(Some(demoJobWithoutSchedulerIdent))
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

object DemoResultClient {

  @JSExportTopLevel("client.DemoResultClient.main")
  def main(context: String): Unit = {
    println(s"DemoResultClient $context")
    DemoResultClient(context).create()
  }
}


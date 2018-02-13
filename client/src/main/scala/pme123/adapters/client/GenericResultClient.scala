package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw._
import play.api.libs.json.JsValue

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

case class GenericResultClient(context: String)
  extends AdaptersClient {

  private lazy val socket = ClientWebsocket(uiState, context)

  @dom
  protected def render: Binding[HTMLElement] = {
    <div>
      {adapterContainer.bind}{//
      ServerServices(uiState, context).jobConfigs().bind}
    </div>
  }


  @dom
  private def adapterContainer = {
    val selectedJobConfig = uiState.selectedJobConfig.bind
    socket.connectWS(selectedJobConfig)
    val lastResult = uiState.lastResults.bind
    <div class="ui main text container">
      <div id="log-panel" class="ui relaxed divided list">
        {Constants(lastResult: _*).map(resultEntry(_).bind)}
      </div>
    </div>
  }

  @dom
  private def resultEntry(genericResult: JsValue) =
    <div class="item">
      {genericResult.toString()}
    </div>

}

object GenericResultClient {

  @JSExportTopLevel("client.GenericResultClient.main")
  def main(context: String): Unit = {
    println(s"GenericResultClient $context")
    GenericResultClient(context).create()
  }
}


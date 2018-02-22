package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw._
import play.api.libs.json.{JsResult, JsValue, Json}
import pme123.adapters.client.ToConcreteResults.ConcreteResult
import pme123.adapters.client._
import pme123.adapters.shared._
import pme123.adapters.shared.demo.DemoResult
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
    val imageElems = demoUIState.imageElems.bind
    <div>
      {Constants(imageElems: _*)
      .map(_.imageElement.bind)}
    </div>
  }
}

object DemoResultClient
  extends ClientImplicits
    with Logger {
  
  LoggerConfig.factory = ConsoleLoggerFactory()

  // type class instance for ImageElem
  @JSExportTopLevel("client.ProjectClient.main")
  def main(context: String, websocketPath: String, clientType: String): Unit = {
    info(s"JobCockpitClient $clientType: $context$websocketPath")
    ClientType.fromString(clientType) match {
      case CUSTOM_PAGE =>
        DemoResultClient(context, websocketPath).create()
      case JOB_PROCESS =>
        JobProcessClient(context, websocketPath).create()
      case JOB_RESULTS =>
        JobResultsClient(context, websocketPath)(DemoResultForJobResultsRow).create()
      case other => warn(s"Unexpected ClientType: $other")
    }
  }
}

trait ClientImplicits {

  implicit object DemoResultForJobResultsRow extends ConcreteResult[JobResultsRow] {

    override def fromJson(lastResult: JsValue): JsResult[JobResultsRow] =
      Json.fromJson[DemoResult](lastResult)
        .map(dr => JobResultsRow(Seq(dr.name, dr.imgUrl, dr.created)))
  }

}


package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.client.demo.DemoClient.warn
import pme123.adapters.shared._
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * if you only want the standard JobCockpit
  *  (no JobResults as table and no custom Page)
  *  you can use this page - so you don't need a client at all!
  *  Configure your project like:
  *   project.config {
  *     ...
  *     client.name = "DefaultClient"
  *     ...
  *     }
  */
object DefaultClient {

  LoggerConfig.factory = ConsoleLoggerFactory()

  @JSExportTopLevel("client.DefaultClient.main")
  def mainPage(context: String
                   , websocketPath: String
                   , clientType: String): Unit = {
    ClientType.fromString(clientType) match {
      case JOB_PROCESS =>
        val socket = ClientWebsocket(context)
        JobProcessView(socket, context, websocketPath, DefaultRunJobDialog(socket)).create()
      case JOB_RESULTS =>
        DefaultView("there is no JobResults page defined").create()
      case CUSTOM_PAGE =>
        DefaultView("there is no custom page defined").create()
      case other => warn(s"Unexpected ClientType: $other")
    }

  }
}

case class DefaultView(message: String)
  extends AdaptersClient {

  @dom
  protected def render: Binding[HTMLElement] =
    <h3>
      <b>
        {message}
      </b>
    </h3>
}
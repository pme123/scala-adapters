package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.ClientType._
import pme123.adapters.shared._
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * if you only want the standard JobCockpit
  * (no JobResults as table and no custom Page)
  * you can use this page - so you don't need a client at all!
  * Configure your project like:
  *   project.config {
  * ...
  *     client.name = "DefaultClient"
  * ...
  * }
  */
object DefaultClient
  extends Logger {

  LoggerConfig.factory = ConsoleLoggerFactory()

  @JSExportTopLevel("client.DefaultClient.main")
  def mainPage(context: String
               , websocketPath: String
               , clientType: String): Unit = {
    info(s"DemoClient $clientType: $context$websocketPath")
    UIStore.changeWebContext(context)

    ClientType.withNameInsensitiveOption(clientType) match {
      case Some(JOB_PROCESS) =>
        JobProcessView(websocketPath, DefaultRunJobDialog).create()
      case Some(JOB_RESULTS) =>
        DefaultView("there is no JobResults page defined").create()
      case Some(CUSTOM_PAGE) =>
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
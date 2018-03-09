package pme123.adapters.client

import pme123.adapters.client.demo.DemoResultClient.warn
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
        JobProcessView(context, websocketPath).create()
      case CUSTOM_PAGE =>
        warn("there is no custom page defined")
      case JOB_RESULTS =>
        warn("there is no JobResults page defined")
      case other => warn(s"Unexpected ClientType: $other")
    }

  }
}

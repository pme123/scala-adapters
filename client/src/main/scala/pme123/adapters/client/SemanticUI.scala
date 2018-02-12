package pme123.adapters.client

import org.scalajs.jquery.JQuery

import scala.language.implicitConversions
import scala.scalajs.js

/**
  * Created by rendong on 17/1/2.
  */
object SemanticUI {

  // Monkey patching JQuery
  @js.native
  trait SemanticJQuery extends JQuery {
    def dropdown(params: js.Any*): SemanticJQuery = js.native
    def popup(params: js.Any*): SemanticJQuery = js.native
    def modal(params: js.Any*): SemanticJQuery = js.native
  }

  // Monkey patching JQuery with implicit conversion
  implicit def jq2semantic(jq: JQuery): SemanticJQuery = jq.asInstanceOf[SemanticJQuery]

  import LogLevel._
  def levelClass(logLevel: LogLevel): String =  logLevel match {
    case ERROR => "red warning circle icon"
    case WARN => "orange warning sign icon"
    case INFO => "blue info circle icon"
    case DEBUG => "grey info circle icon"

  }
}
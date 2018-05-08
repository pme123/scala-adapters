package pme123.adapters.client

import org.scalajs.jquery.JQuery
import pme123.adapters.shared.LogLevel
import pme123.adapters.shared.LogLevel._

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  *
  */
object SemanticUI {

  // Monkey patching JQuery
  @js.native
  trait SemanticJQuery extends JQuery {
    def dropdown(params: js.Any*): SemanticJQuery = js.native
    def popup(params: js.Any*): SemanticJQuery = js.native
    def modal(params: js.Any*): SemanticJQuery = js.native
    def checkbox(params: js.Any*): SemanticJQuery = js.native
    def form(params: js.Any*): SemanticJQuery = js.native

  }

  // Monkey patching JQuery with implicit conversion
  implicit def jq2semantic(jq: JQuery): SemanticJQuery = jq.asInstanceOf[SemanticJQuery]

  def levelClass(logLevel: LogLevel): String =  logLevel match {
    case ERROR => "red warning circle icon"
    case WARN => "orange warning sign icon"
    case INFO => "blue info circle icon"
    case DEBUG => "grey info circle icon"

  }

  @ScalaJSDefined
  trait Form extends js.Object {
    def fields: js.Object
  }

  @ScalaJSDefined
  trait Field extends js.Object {
    def identifier: String
    def rules: js.Array[Rule]
  }

  @ScalaJSDefined
  trait Rule extends js.Object {
    def `type`: String
    def prompt: js.UndefOr[String] = js.undefined
  }
}
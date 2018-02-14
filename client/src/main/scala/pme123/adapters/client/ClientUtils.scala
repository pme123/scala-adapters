package pme123.adapters.client

import java.time.Instant

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.LogEntry

import scala.language.implicitConversions
import scala.scalajs.js

trait ClientUtils
  extends IntellijImplicits {

  def jsLocalDateTime(instant:Instant):String = {
    val date = new js.Date(1000.0 * instant.getEpochSecond)

    s"${date.toLocaleDateString} ${date.toLocaleTimeString}"
  }

  @dom
  def logLevelIcon(entry: LogEntry): Binding[HTMLElement] =
    <i class={"large middle aligned " + SemanticUI.levelClass(entry.level)}></i>

}

trait IntellijImplicits {
  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???
}

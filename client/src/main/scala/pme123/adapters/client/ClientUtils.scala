package pme123.adapters.client

import java.time.Instant

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.LogEntry

import scala.scalajs.js

trait ClientUtils {

  def jsLocalDateTime(instant:Instant):String = {
    val date = new js.Date(1000.0 * instant.getEpochSecond)

    s"${date.toLocaleDateString} ${date.toLocaleTimeString}"
  }

  @dom
  def logLevelIcon(entry: LogEntry): Binding[HTMLElement] =
    <i class={"large middle aligned " + SemanticUI.levelClass(entry.level)}></i>

}

package pme123.adapters.client

import java.time.Instant

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.{LogEntry, Logger}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

trait ClientUtils
  extends IntellijImplicits
    with Logger {

  def jsLocalDateTime(instant: Instant): String = {
    val date = new js.Date(1000.0 * instant.getEpochSecond)

    s"${date.toLocaleDateString} ${date.toLocaleTimeString}"
  }

  // helper - need separate class at some point
  def jsLocalDate(dateString: String): String = {
    val date = new js.Date(dateString)
    date.toLocaleDateString()
  }

  def jsLocalTime(dateString: String): String = {
    val date = new js.Date(dateString)
    if (date.getHours() == 0)
      ""
    else {
      val timeStr = date.toLocaleTimeString
      timeStr.replaceFirst(":\\d\\d\\b", "") // remove seconds
    }
  }

  // get Assets from the servers /public folder
  def staticAsset(path: String): String =
    "" + g.jsRoutes.controllers.Assets.versioned(path).url

  @dom
  def faviconElem: Binding[HTMLElement] =
    <div class="ui item">
      <img src={staticAsset("images/favicon.png")}></img>
    </div>

  @dom
  def logLevelIcon(entry: LogEntry): Binding[HTMLElement] =
    <i class={"large middle aligned " + SemanticUI.levelClass(entry.level)}></i>

  @dom
  def td(value: String): Binding[HTMLElement] =
    <td>
      {value}
    </td>

  @dom
  def tdImg(imageUrl: String): Binding[HTMLElement] =
    <td>
      <img class="defaultImage" src={imageUrl}/>
    </td>

}

trait IntellijImplicits {
  //noinspection NotImplementedCode
  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???
}

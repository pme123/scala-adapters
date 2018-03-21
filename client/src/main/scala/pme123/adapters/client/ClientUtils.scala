package pme123.adapters.client

import java.time.Instant

import com.thoughtworks.binding.{Binding, FutureBinding, dom}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.{JsValue, Json}
import pme123.adapters.shared.{LogEntry, Logger}

import scala.language.implicitConversions
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

trait ClientUtils
  extends IntellijImplicits
    with Logger {

  def jsLocalDateTime(instant:Instant):String = {
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

  @dom
  def logLevelIcon(entry: LogEntry): Binding[HTMLElement] =
    <i class={"large middle aligned " + SemanticUI.levelClass(entry.level)}></i>

  @dom
  def td(value: String): Binding[HTMLElement] =
    <td>{value}</td>

  @dom
  def tdImg(imageUrl: String): Binding[HTMLElement] =
    <td> <img class="defaultImage" src={imageUrl}/></td>

  @dom def callService(apiPath: String, toEntity: (JsValue) => String): Binding[HTMLElement] = {
    FutureBinding(Ajax.get(apiPath))
      .bind match {
      case None =>
        <div class="ui active inverted dimmer front">
          <div class="ui large text loader">Loading</div>
        </div>
      case Some(Success(response)) =>
        val json: JsValue = Json.parse(response.responseText)
        info(s"Json received from $apiPath: ${json.toString().take(20)}")
        <div>
          {toEntity(json)}
        </div>
      case Some(Failure(exception)) =>
        error(exception, s"Problem accessing $apiPath")
        <div>
          {exception.getMessage}
        </div>
    }
  }

}

trait IntellijImplicits {
  //noinspection NotImplementedCode
  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???
}

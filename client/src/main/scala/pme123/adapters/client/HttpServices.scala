package pme123.adapters.client

import com.thoughtworks.binding.{Binding, FutureBinding, dom}
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.{HTMLElement, XMLHttpRequest}
import play.api.libs.json._
import pme123.adapters.shared.Logger

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

trait HttpServices
  extends Logger {

  def httpGet[A](apiPath: String
                  , storeChange: A => Unit)
                 (implicit reads: Reads[A]): Binding[HTMLElement] =
    callService[A](apiPath, Ajax.get(apiPath), storeChange)

  @dom
  def callService[A](apiPath: String
                      , ajaxCall: Future[XMLHttpRequest]
                      , storeChange: A => Unit)
                     (implicit reads: Reads[A]): Binding[HTMLElement] = {
    FutureBinding(ajaxCall)
      .bind match {
      case None =>
        <div class="ui active inverted dimmer front">
          <div class="ui large text loader">Loading</div>
        </div>
      case Some(Success(response)) =>
        val json: JsValue = Json.parse(response.responseText)
        info(s"Json received from $apiPath: ${json.toString().take(20)}")
        <div>
          {json.validate[A]
          .map(storeChange)
          .map(_ => "")
          .recover { case JsError(errors) =>
            error(s"errors: $errors")
            s"Problem parsing ProcessDefinitions: ${errors.map(e => s"${e._1} -> ${e._2}")}"
          }.get}
        </div>
      case Some(Failure(exception: AjaxException)) =>
        val msg = s"Problem accessing $apiPath > ${exception.xhr.status}: ${exception.xhr.statusText}"
        error(exception, msg) //
        <span>
          {errorMessage(msg).bind}
        </span>
      case Some(Failure(exception: Throwable)) =>
        val msg = s"Problem accessing $apiPath > ${exception.getMessage}"
        error(exception, msg) //
        <span>
          {errorMessage(msg).bind}
        </span>
    }
  }

  @dom
  def errorMessage(msg: String): Binding[HTMLElement] =
  //TODO check Message - Semantic-UI (add close button)
    <div class="ui negative floating message">
      <div class="header">
        We're sorry there was an exception on the server!
      </div>
      <p>
        {msg}
      </p>
    </div>

}

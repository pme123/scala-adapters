package pme123.adapters.client

import com.thoughtworks.binding.{Binding, FutureBinding, dom}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.{ClientConfig, JobConfigs}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Created by pascal.mengelt on 16.07.2017.
  */
case class ServerServices(uiState: UIState, context: String)
  extends UIStore {

  def jobConfigs(): Binding[HTMLElement] = {
    val apiPath = s"$context/jobConfigs"

    def toJobConfigs(jsValue: JsValue) = jsValue.validate[JobConfigs] match {
      case JsSuccess(jc, _) =>
        changeJobConfigs(jc)
        // select first if no one is set already
        uiState.selectedJobConfig.value
          .getOrElse(changeSelectedJobConfig(jc.configs.values.headOption))
        ""
      case JsError(errors) =>
        s"Problem parsing JobConfigs: ${errors.map(e => s"${e._1} -> ${e._2}")}"
    }

    callService(apiPath, toJobConfigs)
  }

  def clientConfigs(jobIdent: JobIdent): Binding[HTMLElement] = {
    val apiPath = s"$context/clientConfigs/$jobIdent"

    def toClientConfigs(jsValue: JsValue) = jsValue.validate[List[ClientConfig]] match {
      case JsSuccess(u, _) =>
        replaceAllClients(u)
        ""
      case JsError(errors) =>
        s"Problem parsing List[ClientConfig]: ${errors.map(e => s"${e._1} -> ${e._2}")}"
    }

    callService(apiPath, toClientConfigs)
  }

  @dom private def callService(apiPath: String, toEntity: (JsValue) => String) = {
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

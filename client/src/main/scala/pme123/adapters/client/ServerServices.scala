package pme123.adapters.client

import com.thoughtworks.binding.{Binding, FutureBinding, dom}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.{JsError, JsSuccess, Json}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.{ClientConfig, JobConfigs}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Created by pascal.mengelt on 16.07.2017.
  */
case class ServerServices(uiState: UIState)
  extends UIStore {

  @dom def jobConfigs(context: String): Binding[HTMLElement] = {
    val apiPath = s"$context/jobConfigs"

    FutureBinding(Ajax.get(apiPath))
      .bind match {
      case None =>
        <div class="ui active inverted dimmer front">
          <div class="ui large text loader">Loading</div>
        </div>
      case Some(Success(response)) =>
        val json = Json.parse(response.responseText)
        info(s"Json received JobConfigs: ${json.toString().take(20)}")

        json.validate[JobConfigs] match {
          case JsSuccess(jc, _) =>
            changeJobConfigs(jc)
            // select first if no one is set already
            uiState.selectedJobConfig.value
              .getOrElse(changeSelectedJobConfig(jc.configs.values.headOption))
            <div>
            </div>
          case JsError(errors) =>
            <div>
              {s"Problem parsing JobConfigs: ${errors.map(e => s"${e._1} -> ${e._2}")}"}
            </div>
        }

      case Some(Failure(exception)) =>
        error(exception, s"Problem accessing $apiPath")
        <div>
          {exception.getMessage}
        </div>
    }
  }

  @dom def clientConfigs(context: String, jobIdent: JobIdent): Binding[HTMLElement] = {
    val apiPath = s"$context/clientConfigs/$jobIdent"

    FutureBinding(Ajax.get(apiPath))
      .bind match {
      case None =>
        <div class="ui active inverted dimmer front">
          <div class="ui large text loader">Loading</div>
        </div>
      case Some(Success(response)) =>
        val json = Json.parse(response.responseText)
        info(s"Json received List[AFClientConfig]: ${json.toString().take(20)}")
        json.validate[List[ClientConfig]] match {
          case JsSuccess(u, _) =>
            replaceAllClients(u)
            <div>
            </div>
          case JsError(errors) =>
            <div>
              {s"Problem parsing User: ${errors.map(e => s"${e._1} -> ${e._2}")}"}
            </div>
        }

      case Some(Failure(exception)) =>
        error(exception, s"Problem accessing $apiPath")
        <div>
          {exception.getMessage}
        </div>
    }
  }

}

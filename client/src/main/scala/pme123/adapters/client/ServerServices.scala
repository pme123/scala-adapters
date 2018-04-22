package pme123.adapters.client

import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import pme123.adapters.shared.{ClientConfig, JobConfig}

/**
  * Created by pascal.mengelt on 16.07.2017.
  */
case class ServerServices(context: String)
  extends ClientUtils {

  def jobConfigs(): Binding[HTMLElement] = {
    val apiPath = s"$context/jobConfigs"

    def toJobConfigs(jsValue: JsValue) = jsValue.validate[Seq[JobConfig]] match {
      case JsSuccess(jcs, _) =>
        UIStore.changeJobConfigs(jcs)
        ""
      case JsError(errors) =>
        error(s"errors: $errors")
        s"Problem parsing JobConfigs: ${errors.map(e => s"${e._1} -> ${e._2}")}"
    }

    callService(apiPath, toJobConfigs)
  }

  def clientConfigs(): Binding[HTMLElement] = {
    val apiPath = s"$context/clientConfigs"

    def toClientConfigs(jsValue: JsValue) = jsValue.validate[List[ClientConfig]] match {
      case JsSuccess(u, _) =>
        UIStore.replaceAllClients(u)
        ""
      case JsError(errors) =>
        s"Problem parsing List[ClientConfig]: ${errors.map(e => s"${e._1} -> ${e._2}")}"
    }

    callService(apiPath, toClientConfigs)
  }


}

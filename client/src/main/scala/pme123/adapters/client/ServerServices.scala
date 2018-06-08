package pme123.adapters.client

import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.JsValue
import pme123.adapters.shared.{ClientConfig, JobConfig}

/**
  * Created by pascal.mengelt on 16.07.2017.
  */
case class ServerServices(context: String)
  extends HttpServices {

  def jobConfigs(): Binding[HTMLElement] = {
    val apiPath = s"$context/jobConfigs"

    def toObj(jsValue: JsValue) =
      jsValue.validate[Seq[JobConfig]]
        .map(results => UIStore.changeJobConfigs(results))

    httpGet(apiPath, toObj)
  }

  def clientConfigs(): Binding[HTMLElement] = {
    val apiPath = s"$context/clientConfigs"

    def toObj(jsValue: JsValue) =
      jsValue.validate[Seq[ClientConfig]]
        .map(results => UIStore.replaceAllClients(results))

    httpGet(apiPath, toObj)
  }


}

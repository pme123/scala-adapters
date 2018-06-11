package pme123.adapters.client

import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.{ClientConfig, JobConfig}

/**
  * Created by pascal.mengelt on 16.07.2017.
  */
object ServerServices
  extends HttpServices {

  def jobConfigs(): Binding[HTMLElement] = {
    val apiPath = s"${UIStore.uiState.webContext.value}/jobConfigs"

    httpGet(apiPath, (results: Seq[JobConfig]) => UIStore.changeJobConfigs(results))
  }

  def clientConfigs(): Binding[HTMLElement] = {
    val apiPath = s"${UIStore.uiState.webContext.value}/clientConfigs"

    httpGet(apiPath, (results: Seq[ClientConfig]) => UIStore.replaceAllClients(results))
  }


}

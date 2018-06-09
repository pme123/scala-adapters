package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js.timers.setTimeout
trait AdaptersView
  extends ClientUtils {


  def create(): Unit = {
    dom.render(document.body, render)
  }

  protected def render: Binding[HTMLElement]

  protected def scrollDown(divId: String = "log-container") {
    setTimeout(100) {
      val objDiv = document.getElementById(divId)
      objDiv.scrollTop = objDiv.scrollHeight
    }
  }
}

package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.client.{AdaptersView, UIStore}

object DemoView
  extends AdaptersView {

  @dom
  protected def render: Binding[HTMLElement] = {
    <div>
      {imageContainer.bind}
    </div>
  }

  // 2. level of abstraction
  // **************************
  @dom
  private def imageContainer = {
    val lastResults = UIStore.uiState.lastResults.bind
    val imageElems = DemoUIStore.updateImageElems(lastResults)
    <div>
      {Constants(imageElems: _*)
      .map(_.imageElement.bind)}
    </div>
  }
}

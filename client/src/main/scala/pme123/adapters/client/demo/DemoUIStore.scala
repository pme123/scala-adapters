package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Vars
import play.api.libs.json._
import pme123.adapters.client.ToConcreteResults.ConcreteResult
import pme123.adapters.client.{ToConcreteResults, UIStore}
import pme123.adapters.shared.demo.DemoResult

import scala.language.implicitConversions

trait DemoUIStore
  extends UIStore {

  protected def demoUIState: DemoUIState

  // type class instance for ImageElem
  implicit object concreteResultForImageElem extends ConcreteResult[ImageElem] {

    override def fromJson(lastResult: JsValue): JsResult[ImageElem] =
      Json.fromJson[DemoResult](lastResult)
        .map(ImageElem)
  }

  def updateImageElems(lastResults: Seq[JsValue]): Seq[ImageElem] = {
    ToConcreteResults.toConcreteResults(demoUIState.imageElems, lastResults)
  }
}

case class DemoUIState(
                        imageElems: Vars[ImageElem] = Vars()
                      )

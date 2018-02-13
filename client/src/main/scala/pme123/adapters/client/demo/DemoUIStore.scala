package pme123.adapters.client.demo

import com.thoughtworks.binding.Binding.Vars
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import pme123.adapters.client.UIStore
import pme123.adapters.shared.demo.DemoResult

trait DemoUIStore extends UIStore {

  protected def demoUIState: DemoUIState

  def updateImageElems(lastResults: Seq[JsValue]): Seq[ImageElem] = {
    info("update last results")
    if (lastResults.isEmpty) {
      info("update clear last results")
      demoUIState.imageElems.value.clear()
    }
    else if (lastResults.size - demoUIState.imageElems.value.size == 1) {
      info("update append one last results")
      demoUIState.imageElems.value ++= fromJson(lastResults.last)
    }
    else {
      info("update all last results")
      demoUIState.imageElems.value ++= lastResults.flatMap(fromJson)
    }
    info(s"demoImageElems: ${demoUIState.imageElems.value.size}")
    demoUIState.imageElems.value
  }

  private def fromJson(lastResult: JsValue): List[ImageElem] =
    Json.fromJson[DemoResult](lastResult) match {
      case JsSuccess(jc: DemoResult, _)
      =>
        List(ImageElem(jc))
      case JsError(errors)
      =>
        error(s"Problem parsing DemoResult: ${errors.map(e => s"${e._1} -> ${e._2}")}")
        Nil
    }

  protected def changeImageElems(demoResults: Seq[DemoResult]) {
    info(s"UIStore: changeImageElems ${demoResults.map(_.name)}")
    demoUIState.imageElems.value.clear
    demoUIState.imageElems.value.append(demoResults.map(ImageElem): _*)
  }

  protected def addImageData(imageData: DemoResult) {
    info(s"UIStore: addImageElem $imageData")
    demoUIState.imageElems.value.append(ImageElem(imageData))
  }

}

case class DemoUIState(imageElems: Vars[ImageElem] = Vars())

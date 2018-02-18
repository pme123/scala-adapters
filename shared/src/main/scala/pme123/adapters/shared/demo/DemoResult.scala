package pme123.adapters.shared.demo

import play.api.libs.json.{JsValue, Json, OFormat}
import pme123.adapters.shared.{AConcreteResult, ClientConfig}

case class DemoResults(demoResults: Seq[DemoResult]) extends AConcreteResult {
  override def filter(clientConfig: ClientConfig): Boolean = true

  override def toJson: JsValue = Json.toJson(this)
}

object DemoResults {
  implicit val jsonFormat: OFormat[DemoResults] = Json.format[DemoResults]
}


case class DemoResult(name:String, imgUrl: String) {
}

object DemoResult {
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]
}
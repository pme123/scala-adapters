package pme123.adapters.shared.demo

import play.api.libs.json.{JsValue, Json, OFormat}
import pme123.adapters.shared.{AConcreteResult, ClientConfig}

case class DemoResult(name:String, imgUrl: String) extends AConcreteResult {
  override def filter(clientConfig: ClientConfig): Boolean = true

  override def toJson: JsValue = Json.toJson(this)
}

object DemoResult {
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]
}
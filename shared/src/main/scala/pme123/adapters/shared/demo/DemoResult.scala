package pme123.adapters.shared.demo

import play.api.libs.json.{Json, OFormat}

case class DemoResult(name:String, imgUrl: String)

object DemoResult {
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]
}
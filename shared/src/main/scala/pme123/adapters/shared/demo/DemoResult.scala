package pme123.adapters.shared.demo

import play.api.libs.json.{Json, OFormat}
import pme123.adapters.shared.DateTimeString

case class DemoResult(name: String, imgUrl: String, created: DateTimeString)

object DemoResult {
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]
}
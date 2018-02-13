package pme123.adapters.shared.demo

import play.api.libs.json.{Json, OFormat}
import pme123.adapters.shared.LogLevel

case class DemoResult(logLevel: LogLevel)

object DemoResult {
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]
}
package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait ClientType

object ClientType
  extends Logger {

  def fromString(name: String): ClientType = name match {
    case "JOB_PROCESS" => JOB_PROCESS
    case "JOB_RESULTS" => JOB_RESULTS
    case "CUSTOM_PAGE" => CUSTOM_PAGE
    case other =>
      error(s"Unexpected ClientType name: $other")
      JOB_PROCESS
  }

  implicit val jsonFormat: OFormat[ClientType] = derived.oformat[ClientType]()
}

case object JOB_PROCESS extends ClientType

case object JOB_RESULTS extends ClientType

case object CUSTOM_PAGE extends ClientType

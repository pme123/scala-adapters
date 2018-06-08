package pme123.adapters.shared

import enumeratum.{Enum, EnumEntry}
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait ClientType extends EnumEntry

// see https://github.com/lloydmeta/enumeratum#usage
object ClientType
  extends Enum[ClientType]
    with Logger {

  val values = findValues

  case object JOB_PROCESS extends ClientType

  case object JOB_RESULTS extends ClientType

  case object CUSTOM_PAGE extends ClientType

  @deprecated("use ClientType.withName(name)")
  def fromString(name: String): ClientType = ClientType.withName(name)

  implicit val jsonFormat: OFormat[ClientType] = derived.oformat[ClientType]()
}

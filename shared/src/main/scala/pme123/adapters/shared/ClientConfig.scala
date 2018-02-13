package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat

case class ClientConfig(requestIdent: ClientConfig.RequestIdent
                        , info: String)

object ClientConfig {
  type RequestIdent = String

  implicit val jsonFormat: OFormat[ClientConfig] = derived.oformat[ClientConfig]()

}

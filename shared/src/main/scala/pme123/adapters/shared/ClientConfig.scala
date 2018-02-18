package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.JobConfigTempl.JobIdent

case class ClientConfig(requestIdent: ClientConfig.RequestIdent
                        , jobIdent: JobIdent
                        , clientParams: Map[String, ClientConfig.ClientProperty] = Map()) {
}

object ClientConfig {
  type RequestIdent = String

  type ClientProperty = String

  implicit val jsonFormat: OFormat[ClientConfig] = derived.oformat[ClientConfig]()

}

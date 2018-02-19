package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.JobConfig.JobIdent

case class ClientConfig(requestIdent: ClientConfig.RequestIdent
                        , jobIdent: JobIdent
                        , clientParams: Map[String, ClientConfig.ClientProperty] = Map()) {
}

object ClientConfig {
  type RequestIdent = String

  type ClientProperty = String

  val encoding = "UTF-8"

  implicit val jsonFormat: OFormat[ClientConfig] = derived.oformat[ClientConfig]()

}

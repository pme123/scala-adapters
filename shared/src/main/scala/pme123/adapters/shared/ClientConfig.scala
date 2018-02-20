package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat

case class ClientConfig(requestIdent: ClientConfig.RequestIdent
                        , jobConfig: JobConfig
                        , resultCount: Int = ClientConfig.defaultResultCount
                        , resultFilter: Option[String] = None) {
}

object ClientConfig {
  type RequestIdent = String

  val encoding = "UTF-8"

  val defaultResultCount = 20

  implicit val jsonFormat: OFormat[ClientConfig] = derived.oformat[ClientConfig]()

}

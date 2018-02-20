package pme123.adapters.server.entity

import play.api.libs.json.JsValue
import pme123.adapters.shared.ClientConfig

trait AConcreteResult {

  def clientFiltered(clientConfig: ClientConfig): Seq[JsValue]
  
}

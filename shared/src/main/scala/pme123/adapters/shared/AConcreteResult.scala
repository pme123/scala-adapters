package pme123.adapters.shared

import play.api.libs.json.JsValue


trait AConcreteResult {

  def filter(clientConfig: ClientConfig): Boolean

  def toJson: JsValue

}
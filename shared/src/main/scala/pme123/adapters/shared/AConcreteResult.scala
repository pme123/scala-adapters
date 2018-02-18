package pme123.adapters.shared

import play.api.libs.json.JsValue


trait AConcreteResult {

  def clientFiltered(clientConfig: ClientConfig): AConcreteResult

  def toJson: JsValue

}
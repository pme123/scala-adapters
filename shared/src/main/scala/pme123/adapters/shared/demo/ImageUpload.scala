package pme123.adapters.shared.demo

import julienrf.json.derived
import play.api.libs.json.OFormat

case class ImageUpload(descr: String, imgData: String)

object ImageUpload {
  implicit val jsonFormat: OFormat[ImageUpload] = derived.oformat[ImageUpload]()

}

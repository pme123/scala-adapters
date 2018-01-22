package pme123.adapters.shared

import java.time.Instant

import play.api.libs.json.{JsNumber, JsValue, Reads, Writes}

trait InstantHelper {
  implicit val localInstantReads: Reads[Instant] =
    (json: JsValue) => {
      json.validate[Long]
        .map { epochSecond =>
          Instant.ofEpochSecond(epochSecond)
        }
    }

  implicit val localInstantWrites: Writes[Instant] =
    (instant: Instant) => JsNumber(instant.getEpochSecond)

}

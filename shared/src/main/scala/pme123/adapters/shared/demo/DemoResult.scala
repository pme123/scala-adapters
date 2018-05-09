package pme123.adapters.shared.demo

import play.api.libs.json._
import pme123.adapters.shared.DateTimeString

case class DemoResult(name: String, img: Either[DemoResult.ImgData, DemoResult.ImgData], created: DateTimeString)

object DemoResult {
  type ImgUrl = String
  type ImgData = String
  implicit val jsonFormat: OFormat[DemoResult] = Json.format[DemoResult]

  implicit def eitherReads[A, B](implicit A: Reads[A], B: Reads[B]): Reads[Either[A, B]] =
    Reads[Either[A, B]] { json =>
      A.reads(json) match {
        case JsSuccess(value, path) => JsSuccess(Left(value), path)
        case JsError(e1) => B.reads(json) match {
          case JsSuccess(value, path) => JsSuccess(Right(value), path)
          case JsError(e2) => JsError(JsError.merge(e1, e2))
        }
      }
    }

  implicit def eitherWrites[A, B](implicit A: Writes[A], B: Writes[B]): Writes[Either[A,B]] =
    Writes[Either[A, B]] {
      case Left(a) => A.writes(a)
      case Right(b) => B.writes(b)
    }

  implicit def eitherFormat[A, B](implicit A: Format[A], B: Format[B]): Format[Either[A,B]] =
    Format(eitherReads, eitherWrites)
}
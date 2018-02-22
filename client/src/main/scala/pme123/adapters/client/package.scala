package pme123.adapters

import play.api.libs.json.{JsResult, JsValue, Json}
import pme123.adapters.client.ToConcreteResults.ConcreteResult
import pme123.adapters.shared.demo.DemoResult

package object client {
  // type class instance for ImageElem
  implicit object concreteResultForJobResultsRow extends ConcreteResult[JobResultsRow] {

    override def fromJson(lastResult: JsValue): JsResult[JobResultsRow] =
      Json.fromJson[DemoResult](lastResult)
        .map(dr => JobResultsRow(Seq(dr.name, dr.imgUrl, dr.created)))
  }
}

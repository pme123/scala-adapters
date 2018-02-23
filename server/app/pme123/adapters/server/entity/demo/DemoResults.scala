package pme123.adapters.server.entity.demo

import play.api.libs.json.{JsValue, Json}
import pme123.adapters.server.entity.AConcreteResult
import pme123.adapters.shared.ClientConfig.Filterable
import pme123.adapters.shared.{ClientConfig, dateTimeAfterL, dateTimeBeforeL}
import pme123.adapters.shared.demo.DemoResult

case class DemoResults(results: Seq[DemoResult]) extends AConcreteResult {

  // type class instance for ImageElem
  implicit object filterableDemoResult extends Filterable[DemoResult] {
    def sortBy(filterable: DemoResult): String = filterable.name

    def doFilter(filterable: DemoResult)(implicit filters: Map[String, String]): Boolean = {
      matchText("name", filterable.name) &&
        matchText("imgUrl", filterable.imgUrl) &&
        matchDate(dateTimeAfterL, filterable.created, (a, b) => a <= b) &&
        matchDate(dateTimeBeforeL, filterable.created, (a, b) => a >= b)
    }

  }

  def clientFiltered(clientConfig: ClientConfig): Seq[JsValue] =
    ClientConfig.filterResults(results, clientConfig)
      .map(dr => Json.toJson(dr))
}

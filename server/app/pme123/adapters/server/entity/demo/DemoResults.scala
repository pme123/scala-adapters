package pme123.adapters.server.entity.demo

import play.api.libs.json.{JsValue, Json}
import pme123.adapters.server.entity.AConcreteResult
import pme123.adapters.shared.ClientConfig.Filterable
import pme123.adapters.shared.{ClientConfig, Logger, dateTimeAfterL, dateTimeBeforeL}
import pme123.adapters.shared.demo.DemoResult

case class DemoResults(results: Seq[DemoResult])
  extends AConcreteResult
    with Logger {

  // type class instance for DemoResult
  implicit object filterableDemoResult extends Filterable[DemoResult] {
    def sortBy(filterable: DemoResult): String = filterable.name

    def doFilter(filterable: DemoResult)(implicit filters: Map[String, String]): Boolean = {
      matchText("name", filterable.name) &&
        matchText("imgUrl", s"${if(filterable.img.isLeft) filterable.img.left else filterable.img.right}") &&
        matchDate(dateTimeAfterL, filterable.created, (a, b) => a <= b) &&
        matchDate(dateTimeBeforeL, filterable.created, (a, b) => a >= b)
    }

  }

  // method to filter the results for a ClientConfig
  def clientFiltered(clientConfig: ClientConfig): Seq[JsValue] =
    ClientConfig.filterResults(results, clientConfig)
      .map(dr => Json.toJson(dr))

  def merge(other: AConcreteResult): AConcreteResult = other match {
    case DemoResults(toMerge) =>
      DemoResults(results ++ toMerge)
    case unexpected =>
      warn(s"Not expected message: $unexpected")
      this
  }
}

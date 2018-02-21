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
  val resultCountL = "resultCount"
  val resultFilterL = "resultFilter"

  val defaultResultCount = 20

  implicit val jsonFormat: OFormat[ClientConfig] = derived.oformat[ClientConfig]()

  // type class
  trait Filterable[A] {
    def sortBy(filterable: A): String

    def doFilter(filterable: A)(implicit filters: Map[String, String]): Boolean

    protected def matchText(label: String, text: String)
                           (implicit filters: Map[String, String]): Boolean = {
      filters.get(label)
        .map(_.toLowerCase.replace("*", ".*"))
        .forall(text.toLowerCase.matches)
    }

    protected def matchDate(label: String, text: String
                            , compareCondition: (Int, Int) => Boolean)
                           (implicit filters: Map[String, String]): Boolean = {
      filters.get(label)
        .map(_.compareTo(text))
        .forall(compareCondition(_, 0))
    }
  }

  def filterResults[A: Filterable](filterables: Seq[A]
                                   , clientConfig: ClientConfig)
                                  (implicit filterable: Filterable[A]): Seq[A] = {
    implicit val filters: Map[String, String] = clientConfig.resultFilter
      .map(_.split(","))
      .map(extractFilters).getOrElse(Map())

    filterables
      .filter(filterable.doFilter)
      .sortBy(filterable.sortBy)
      .take(clientConfig.resultCount)
  }


  private def extractFilters(filterElems: Array[String]): Map[String, String] = {
    filterElems
      .map(_.trim)
      .filter(_.nonEmpty)
      .map { elem: String =>
        extractFilter(elem)
      }.toMap
  }

  private def extractFilter(elem: String): (String, String) = {
    elem.split("=").toList match {
      case x :: y :: _ => (x, y)
      case _ => ("-", "-")
    }
  }

}

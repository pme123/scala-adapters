package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.JobConfigTempl.JobIdent

case class JobConfigTempls(configs: Map[JobIdent, JobConfigTempl]) {

  def fromIdent(ident: JobIdent): Option[JobConfigTempl] =
    configs
      .values
      .find(_.ident == ident)

}

object JobConfigTempls {
  implicit val jsonFormat: OFormat[JobConfigTempls] = derived.oformat[JobConfigTempls]()


}

case class JobConfigTempl(ident: JobConfigTempl.JobIdent
                          , jobSchedule: Option[ScheduleConfig] = None
                        )

object JobConfigTempl {

  type JobIdent = String
  implicit val jsonFormat: OFormat[JobConfigTempl] = derived.oformat[JobConfigTempl]()

}

case class ScheduleConfig(firstTime: String, intervalInMin: Int, firstWeekDay: Option[String] = None)

object ScheduleConfig {
  implicit val jsonFormat: OFormat[ScheduleConfig] = derived.oformat[ScheduleConfig]()


}
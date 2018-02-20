package pme123.adapters.shared

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.JobConfig.JobIdent

case class JobConfigs(configs: Seq[JobConfig] = Nil) {

  def fromIdent(ident: JobIdent): Option[JobConfig] =
    configs
      .find(_.jobIdent == ident)

}

object JobConfigs {
  implicit val jsonFormat: OFormat[JobConfigs] = derived.oformat[JobConfigs]()

}

case class JobConfig(jobIdent: JobIdent
                     , jobSchedule: Option[ScheduleConfig] = None
                     , subWebPath: String = "") {

  def webPath: String = s"/$jobIdent$subWebPath"
}

object JobConfig {

  type JobIdent = String
  implicit val jsonFormat: OFormat[JobConfig] = derived.oformat[JobConfig]()

}

case class ScheduleConfig(firstTime: String, intervalInMin: Int, firstWeekDay: Option[String] = None)

object ScheduleConfig {
  implicit val jsonFormat: OFormat[ScheduleConfig] = derived.oformat[ScheduleConfig]()


}
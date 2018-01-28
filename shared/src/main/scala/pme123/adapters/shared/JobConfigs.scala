package pme123.adapters.shared

import java.time.Instant

import julienrf.json.derived
import play.api.libs.json.OFormat

case class JobConfigs(configs: Map[String, JobConfig]) {

  def fromIdent(ident: String): Option[JobConfig] =
    configs
      .values
      .find(_.ident == ident)

}

object JobConfigs {
  implicit val jsonFormat: OFormat[JobConfigs] = derived.oformat[JobConfigs]()


}

case class JobConfig(ident: JobConfig.JobIdent
                         , jobSchedule: Option[JobSchedule] = None
                        )

object JobConfig {

  type JobIdent = String
  implicit val jsonFormat: OFormat[JobConfig] = derived.oformat[JobConfig]()

}

case class JobSchedule(firstTime: Instant, intervalInMin: Int, firstWeekDay: Option[String] = None)

object JobSchedule extends InstantHelper {
  implicit val jsonFormat: OFormat[JobSchedule] = derived.oformat[JobSchedule]()

}
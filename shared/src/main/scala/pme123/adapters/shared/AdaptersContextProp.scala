package pme123.adapters.shared

import java.time.Instant

import julienrf.json.derived
import play.api.libs.json.OFormat
import pme123.adapters.shared.JobConfig.JobIdent

case class AdaptersContextProps(key: String, props: Seq[AdaptersContextProp])

object AdaptersContextProps {
  implicit val jsonFormat: OFormat[AdaptersContextProps] = derived.oformat[AdaptersContextProps]()

}

case class AdaptersContextProp(key: String, value: String) {

  def asString(name: String): String =
    propString(name)

  def propString(name: String) =
    s"${name.toUpperCase} '$key' >> $value"

}

object AdaptersContextProp {
  implicit val jsonFormat: OFormat[AdaptersContextProp] = derived.oformat[AdaptersContextProp]()

}

case class SchedulerInfo(jobIdent: JobIdent
                         , nextExecution: Instant
                         , firstWeekday: String
                         , periodInMin: Double)

object SchedulerInfo extends InstantHelper {
  implicit val jsonFormat: OFormat[SchedulerInfo] = derived.oformat[SchedulerInfo]()

}
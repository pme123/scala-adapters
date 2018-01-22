package pme123.adapters.shared

import java.time.Instant

import julienrf.json.derived
import play.api.libs.json.OFormat

case class AdaptersContextProp(key: String, value: String)

object AdaptersContextProp {
  implicit val jsonFormat: OFormat[AdaptersContextProp] = derived.oformat[AdaptersContextProp]()

}

case class SchedulerInfo(nextExecution: Instant
                         , firstWeekday: String
                         , periodInMin: Long)

object SchedulerInfo extends InstantHelper {
  implicit val jsonFormat: OFormat[SchedulerInfo] = derived.oformat[SchedulerInfo]()

}
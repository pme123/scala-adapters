package pme123.adapters.server.entity

import java.time.ZoneId

import com.typesafe.config.Config
import pme123.adapters.shared.{JobConfig, ScheduleConfig}

case class JobConfigCreator(config: Config, timezone: ZoneId) {

  import JobConfigCreator._

  def create(): (String, JobConfig) = {
    val ident = config.getString(jobIdentProp)
    ident -> JobConfig(
      ident
      , if (config.hasPath(scheduleProp))
        Some(ScheduleConfigCreator(config.getConfig(scheduleProp), timezone).create())
      else
        None
    )
  }

}

object JobConfigCreator {
  val scheduleProp = "schedule"
  val jobIdentProp = "ident"

}

case class ScheduleConfigCreator(config: Config, timezone: ZoneId) {

  import ScheduleConfigCreator._

  private val firstTimeString =
    if (config.hasPath(firstTimeProp)) config.getString(firstTimeProp) else defaultFirstTime

  private val intervalInMin =
    if (config.hasPath(intervalInMinProp)) config.getInt(intervalInMinProp) else defaultIntervalInMin

  private val firstWeekday =
    if (config.hasPath(firstWeekdayProp)) Some(config.getString(firstWeekdayProp)) else defaultFirstWeekday


  def create(): ScheduleConfig = {
    ScheduleConfig(
      firstTimeString
      , intervalInMin
      ,firstWeekday
    )
  }
}

object ScheduleConfigCreator {
  val firstTimeProp = "first.time"
  val firstWeekdayProp = "first.weekday"
  val intervalInMinProp = "interval.minutes"
  val defaultFirstTime = "01:00"
  val defaultIntervalInMin = 1440 // 1 day
  val defaultFirstWeekday: Option[String] = None

}
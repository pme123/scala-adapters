package pme123.adapters.server.entity

import java.time.{DayOfWeek, LocalDate, LocalDateTime, ZoneId}

import com.typesafe.config.Config
import pme123.adapters.shared.{JobConfig, JobSchedule}

case class JobConfigCreator(config: Config, timezone: ZoneId) {

  import JobConfigCreator._

  def create(): (String, JobConfig) = {
    val ident = config.getString(jobIdentProp)
    ident -> JobConfig(
      ident
      , if (config.hasPath(scheduleProp))
        Some(JobScheduleCreator(config.getConfig(scheduleProp), timezone).create())
      else
        None
    )
  }

}

object JobConfigCreator {
  val scheduleProp = "schedule"
  val jobIdentProp = "ident"

}

case class JobScheduleCreator(config: Config, timezone: ZoneId) {

  import JobScheduleCreator._

  private val intervalInMin =
    if (config.hasPath(intervalInMinProp)) config.getInt(intervalInMinProp) else defaultIntervalInMin

  private val firstWeekday =
    if (config.hasPath(firstWeekdayProp)) Some(config.getString(firstWeekdayProp)) else defaultFirstWeekday


  def create(): JobSchedule = {
    JobSchedule(
      firstTime
      , intervalInMin
      ,firstWeekday
    )
  }

  // for testing
  private[entity] def dayOfWeek: Option[DayOfWeek] = {
    firstWeekday flatMap {
      case "monday" => Some(DayOfWeek.MONDAY)
      case "tuesday" => Some(DayOfWeek.TUESDAY)
      case "wednesday" => Some(DayOfWeek.WEDNESDAY)
      case "thursday" => Some(DayOfWeek.THURSDAY)
      case "friday" => Some(DayOfWeek.FRIDAY)
      case "saturday" => Some(DayOfWeek.SATURDAY)
      case "sunday" => Some(DayOfWeek.SUNDAY)
      case _ => None
    }
  }

  // for testing
  private[entity] def now = LocalDate.now

  private[entity] def firstTime = {
     val firstTimeStr =
    if (config.hasPath(firstTimeProp)) config.getString(firstTimeProp) else defaultFirstTime

    val offset = dayOfWeek
      .map { dow =>
        val weekDay = dow.getValue
        val currentDay = now.getDayOfWeek.getValue
        if (weekDay >= currentDay)
          weekDay - currentDay
        else
          7 - currentDay + weekDay
      }.getOrElse(0)
    val ldt = LocalDateTime
      .of(now, DateTimeHelper.toTime(firstTimeStr))
      .plusDays(offset)
    val zo = timezone.getRules.getOffset(ldt)
    ldt.toInstant(zo)
  }
}

object JobScheduleCreator {
  val firstTimeProp = "first.time"
  val firstWeekdayProp = "first.weekday"
  val intervalInMinProp = "interval.minutes"
  val defaultFirstTime = "01:00"
  val defaultIntervalInMin = 1440 // 1 day
  val defaultFirstWeekday: Option[String] = None

}
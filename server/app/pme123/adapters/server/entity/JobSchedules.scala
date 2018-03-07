package pme123.adapters.server.entity

import java.time.{DayOfWeek, Instant, LocalDate, LocalDateTime}

import pme123.adapters.server.entity.AdaptersContext.settings.{jobConfigs, timezoneID}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.ScheduleConfig

import scala.concurrent.duration._

case class JobSchedules(schedules: Map[JobIdent, JobSchedule]) {

  def jobSchedule(ident: JobIdent): JobSchedule =
    schedules.getOrElse(ident, throw BadArgumentException(s"There is no Schedule for the JobIdent: $ident"))
}

object JobSchedules {
  def apply(): JobSchedules = new JobSchedules(
    jobConfigs
      .filter(entry => entry._2.jobSchedule.nonEmpty)
      .map(entry => entry._1 -> JobSchedule(entry._1, entry._2.jobSchedule.get))
  )
}

case class JobSchedule(jobIdent: JobIdent, scheduleConfig: ScheduleConfig) {

  private val minIntervalInMin = 1
  val intervalInMin: Long = Math.max(minIntervalInMin, scheduleConfig.intervalInMin).asInstanceOf[Long]

  val intervalDuration: FiniteDuration = intervalInMin.minutes

  // parameters for testing
  def firstTime(now: LocalDate = LocalDate.now(timezoneID)
                , maybeDayOfWeek: Option[DayOfWeek] = dayOfWeek()): Instant = {
    val ldt = {
        val offset =
          maybeDayOfWeek
            .map { dow =>
              val weekDay = dow.getValue
              val currentDay = now.getDayOfWeek.getValue
              if (weekDay >= currentDay)
                weekDay - currentDay
              else
                7 - currentDay + weekDay
            }.getOrElse(0)
        LocalDateTime
          .of(now, DateTimeHelper.toTime(scheduleConfig.firstTime))
          .plusDays(offset)
    }

    val zo = timezoneID.getRules.getOffset(ldt)
    ldt.toInstant(zo)
  }

  private def dayOfWeek(): Option[DayOfWeek] = {
    scheduleConfig.firstWeekDay flatMap {
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

}

object JobSchedule {

}


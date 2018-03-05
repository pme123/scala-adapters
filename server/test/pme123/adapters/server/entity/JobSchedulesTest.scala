package pme123.adapters.server.entity

import java.time.{DayOfWeek, Instant, LocalDate, LocalDateTime}

import pme123.adapters.shared.demo.DemoJobs._
import pme123.adapters.server.entity.AdaptersContext.settings.timezoneID

class JobSchedulesTest
  extends UnitTest
    with TestResources {


  private val expectedStartHour = 3 // "03:00"
  private val schedules = JobSchedules()
  private val nowJobSchedule: JobSchedule = schedules.jobSchedule(demoJobIdent)
  private val jobSchedule = nowJobSchedule.copy(scheduleConfig= nowJobSchedule.scheduleConfig.copy(firstTime = "03:00"))

  "A JobSchedules" should "be init correctly" in {
    schedules.schedules.values.size should be(2)
  }
  it should "return the correct JobSchedule for a JobIdent" in {
    schedules.jobSchedule(demoJobIdent).jobIdent should be(demoJobIdent)
  }
  it should "throw a BadArgumentException for a JobConfig that has no Schedule" in {
    intercept[BadArgumentException](schedules.jobSchedule(demoJobWithoutSchedulerIdent).jobIdent should be(demoJobIdent))
  }

  s"The first time of NOW" should "be now" in {
    val instant = nowJobSchedule.firstTime(LocalDate.now(timezoneID), None)
    assert(Math.abs(Instant.now().getEpochSecond - instant.getEpochSecond) <= 1)
  }

  s"The first time of  $expectedStartHour" should "be correct without Weekday configured" in {
    val instant = jobSchedule.firstTime(testNow, None)
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezoneID)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay)
  }
  it should "be correct with today configured" in {
    val instant = jobSchedule.firstTime(testNow, Some(DayOfWeek.THURSDAY))
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezoneID)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay)
  }
  it should "be correct with Friday configured" in {
    val instant = jobSchedule.firstTime(testNow, Some(DayOfWeek.FRIDAY))
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezoneID)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay + 1)
  }
  it should "be correct with Monday configured" in {
    val instant = jobSchedule.firstTime(testNow, Some(DayOfWeek.MONDAY))
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezoneID)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay + 4)
  }

  private def testFirstTime(firstTime: LocalDateTime) = {
    assert(firstTime.getSecond === 0)
    assert(firstTime.getMinute === 0)
    assert(firstTime.getHour === expectedStartHour)
    assert(firstTime.getMonthValue === testMonth)
    assert(firstTime.getYear === testYear)
  }
}

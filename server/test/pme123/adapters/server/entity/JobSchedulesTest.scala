package pme123.adapters.server.entity

import java.time._

import pme123.adapters.shared.demo.DemoJobs._
import pme123.adapters.server.entity.AdaptersContext.settings.timezoneID

class JobSchedulesTest
  extends UnitTest
    with TestResources {


  private val expectedStartHour = 3 // "03:00"
  private val schedules = JobSchedules()
  private val jobSchedule: JobSchedule = schedules.jobSchedule(demoJobIdent)

  "A JobSchedules" should "be init correctly" in {
    schedules.schedules.values.size should be(2)
  }
  it should "return the correct JobSchedule for a JobIdent" in {
    schedules.jobSchedule(demoJobIdent).jobIdent should be(demoJobIdent)
  }
  it should "throw a ConfigException for a JobConfig that has no Schedule" in {
    intercept[ConfigException](schedules.jobSchedule(demoJobWithoutSchedulerIdent).jobIdent should be(demoJobIdent))
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

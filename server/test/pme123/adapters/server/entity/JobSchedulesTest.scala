package pme123.adapters.server.entity

import java.time.{DayOfWeek, LocalDateTime}

import pme123.adapters.server.control.demo.DemoJobFactory._
import pme123.adapters.server.entity.AdaptersContext.settings.timezoneID

class JobSchedulesTest
  extends UnitTest
    with TestResources {


  private val expectedStartHour = 3 // "03:00"
  private val schedules = JobSchedules()
  private val jobSchedule = schedules.jobSchedule(demoJobIdent)

  "A JobSchedules" should "be init correctly" in {
    schedules.schedules.values.size should be(2)
  }
  it should "return the correct JobSchedule for a JobIdent" in {
    schedules.jobSchedule(demoJobIdent).jobIdent should be(demoJobIdent)
  }
  it should "throw a BadArgumentException for a JobConfig that has no Schedule" in {
    intercept[BadArgumentException](schedules.jobSchedule(demoJobWithoutSchedulerIdent).jobIdent should be(demoJobIdent))
  }
  /*
    it should "" in {
      val executionPeriod = 1.day.toMinutes
      scheduler.intervalDuration.length should be(executionPeriod * 60)
      assert(scheduler.firstExecution isAfter Instant.now)
      assert((scheduler.firstExecution.toEpochMilli - Instant.now.toEpochMilli) <= scheduler.intervalDuration.toMillis)
      debug(s"The first import is ${scheduler.firstExecution}")
    }

    it should "init correctly with offset" in {
      val offsetInMin = 60
      val scheduler2 = TestAdapterScheduler(offsetInMin)
      scheduler2.firstExecution should be(scheduler.firstExecution.plusSeconds(offsetInMin * 60))
      debug(s"The first import is ${scheduler2.firstExecution}")
    }*/

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

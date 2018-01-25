package pme123.adapters.server.entity

import java.time.{DayOfWeek, LocalDateTime}

import com.typesafe.config.Config
import pme123.adapters.server.entity.JobConfigCreator.scheduleProp

class JobScheduleCreatorTest extends UnitTest
  with DateTimeHelper
  with TestResources {

  def getScheduleConfig(index: Int): Config = getJobConfig(index).getConfig(scheduleProp)

  runWithSchedule(getScheduleConfig(1), 1)
  runWithSchedule(getScheduleConfig(0), 3)

  def runWithSchedule(scheduleConfig: Config, expectedStartHour: Int) {

    object TestJobScheduleCreator extends JobScheduleCreator(scheduleConfig, timezone) {
      override private[entity] def now = testNow

      override def dayOfWeek: Option[DayOfWeek] = None

    }

    object TestJobScheduleCreatorThursday extends JobScheduleCreator(scheduleConfig, timezone) {
      override private[entity] def now = testNow

      override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.THURSDAY)
    }

    object TestJobScheduleCreatorFriday extends JobScheduleCreator(scheduleConfig, timezone) {
      override private[entity] def now = testNow

      override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.FRIDAY)
    }

    object TestJobScheduleCreatorMonday extends JobScheduleCreator(scheduleConfig, timezone) {
      override private[entity] def now = testNow

      override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.MONDAY)
    }

    s"The first time of  $expectedStartHour" should "be correct without Weekday configured" in {
      val instant = TestJobScheduleCreator.firstTime
      val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
      testFirstTime(firstTime)
      assert(firstTime.getDayOfMonth === testDay)
    }
    it should "be correct with today configured" in {
      val instant = TestJobScheduleCreatorThursday.firstTime
      val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
      testFirstTime(firstTime)
      assert(firstTime.getDayOfMonth === testDay)
    }
    it should "be correct with Friday configured" in {
      val instant = TestJobScheduleCreatorFriday.firstTime
      val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
      testFirstTime(firstTime)
      assert(firstTime.getDayOfMonth === testDay + 1)
    }
    it should "be correct with Monday configured" in {
      val instant = TestJobScheduleCreatorMonday.firstTime
      val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
      testFirstTime(firstTime)
      assert(firstTime.getDayOfMonth === testDay + 4)
    }

    def testFirstTime(firstTime: LocalDateTime) = {
      assert(firstTime.getSecond === 0)
      assert(firstTime.getMinute === 0)
      assert(firstTime.getHour === expectedStartHour)
      assert(firstTime.getMonthValue === testMonth)
      assert(firstTime.getYear === testYear)
    }
  }
}


package pme123.adapters.server.entity

import java.time._

import com.typesafe.config.ConfigFactory

class AdaptersSettingsTest
  extends UnitTest
with DateTimeHelper{

  private val testYear = 2017
  private val testDay = 4
  private val testMonth = 5
  private val testNow = LocalDate.of(testYear, testMonth, testDay)

  object TestAdaptersSettings extends AdaptersSettings(ConfigFactory.load()) {
    override private[entity] def now = testNow
  }

  object TestAdaptersSettingsThursday extends AdaptersSettings(ConfigFactory.load()) {
    override private[entity] def now = testNow

    override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.THURSDAY)
  }

  object TestAdaptersSettingsFriday extends AdaptersSettings(ConfigFactory.load()) {
    override private[entity] def now = testNow

    override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.FRIDAY)
  }

  object TestAdaptersSettingsMonday extends AdaptersSettings(ConfigFactory.load()) {
    override private[entity] def now = testNow

    override def dayOfWeek: Option[DayOfWeek] = Some(DayOfWeek.MONDAY)
  }

  "The first time" should "be correct without Weekday configured" in {
    val instant = TestAdaptersSettings.schedulerExecutionFirstTime
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay)
  }
  it should "be correct with today configured" in {
    val instant = TestAdaptersSettingsThursday.schedulerExecutionFirstTime
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay)
  }
  it should "be correct with Friday configured" in {
    val instant = TestAdaptersSettingsFriday.schedulerExecutionFirstTime
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay + 1)
  }
  it should "be correct with Monday configured" in {
    val instant = TestAdaptersSettingsMonday.schedulerExecutionFirstTime
    val firstTime: LocalDateTime = LocalDateTime.ofInstant(instant, timezone)
    testFirstTime(firstTime)
    assert(firstTime.getDayOfMonth === testDay + 4)
  }

  private def testFirstTime(firstTime: LocalDateTime) = {
    assert(firstTime.getSecond === 0)
    assert(firstTime.getMinute === 0)
    assert(firstTime.getHour === 1)
    assert(firstTime.getMonthValue === testMonth)
    assert(firstTime.getYear === testYear)
  }
}


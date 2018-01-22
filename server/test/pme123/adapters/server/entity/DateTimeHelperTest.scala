package pme123.adapters.server.entity

import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalTime}

import pme123.adapters.server.entity.content.ExceptionMessages

/**
  * Created by pascal.mengelt on 09.03.2015.
  *
  */
class DateTimeHelperTest
  extends UnitTest
    with ExceptionMessages
    with DateTimeHelper {
  // toDayDate
  {
    val dayDateStr = "2005-05-12"

    "The created Date" should "be valid and match" in {
      validateDayDate(toDayDate(dayDateStr))
    }
    it should "throw an IllegalArgumentException if the format is not valid" in {
      val dayDateStr = "200505-12"
      val exc = intercept[IllegalArgumentException] {
        toDayDate(dayDateStr)
      }
      exc.getMessage should be(exceptionRequirementFailed + dayNotCorrectFormat)
    }
    it should "cut the hours from the text" in {
      val dayDateStr = "2005-05-12T12:12Z"
      validateDayDate(toDayDate(dayDateStr))
    }
    it should "throw an DateTimeParseException if the day is not possible" in {
      val dayDateStr = "2005-04-31"
      val exc = intercept[DateTimeParseException] {
        toDayDate(dayDateStr)
      }
      exc.getMessage should be("Text '2005-04-31' could not be parsed: Invalid date 'APRIL 31'")
    }
    it should "throw an IllegalArgumentException if the dateString is null" in {
      val dayDateStr = null
      val exc = intercept[IllegalArgumentException] {
        toDayDate(dayDateStr)
      }
      exc.getMessage should be(exceptionRequirementFailed + dayMustNotBeBlank)
    }
    it should "work even if there are tailing spaces and backslashes" in {
      validateDayDate(toDayDate(" " + dayDateStr + "\t\n\r "))
    }
    def validateDayDate(date: LocalDate) {
      val year = 2005
      date.getYear should be(year)
      val month = 5
      date.getMonth.getValue should be(month)
      val day = 12
      date.getDayOfMonth should be(day)
    }
  }
  // toTimeWithSeconds
  {
    val timeStr = "14:02:20Z"
    "The created Time with seconds" should "be valid and match" in {
      validateTimeWithSeconds(toTime(timeStr))
    }
    it should "throw an DateTimeParseException if the format is not valid" in {
      val timeDateStr = "14:00y"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '14:00y' could not be parsed, unparsed text found at index 5")
    }
    it should "throw an DateTimeParseException if the time is not possible" in {
      val timeDateStr = "25:00:00Z"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '25:00:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25")
    }
    it should "throw an IllegalArgumentException if the dateString is null" in {
      val exc = intercept[IllegalArgumentException] {
        toTime(null)
      }
      exc.getMessage should be(exceptionRequirementFailed + timeMustNotBeBlank)
    }
    it should "work even if there are tailing spaces and backslashes" in {
      validateTimeWithSeconds(toTime(" " + timeStr + "\t\n\r "))
    }

  }
  // toTimeWithSecondsWithoutZ
  {
    val timeStr = "14:02:20"
    "The created Time with seconds without Z" should "be valid and match" in {
      validateTimeWithSeconds(toTime(timeStr))
    }
    it should "throw an DateTimeParseException if the format is not valid" in {
      val timeDateStr = "14:00:20y"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '14:00:20y' could not be parsed, unparsed text found at index 8")
    }
    it should "throw an DateTimeParseException if the time is not possible" in {
      val timeDateStr = "25:00:00"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '25:00:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25")
    }
    it should "throw an IllegalArgumentException if the dateString is blank" in {
      val exc = intercept[IllegalArgumentException] {
        toTime(" ")
      }
      exc.getMessage should be(exceptionRequirementFailed + timeMustNotBeBlank)
    }
    it should "work even if there are tailing spaces and backslashes" in {
      validateTimeWithSeconds(toTime(" " + timeStr + "\t\n\r "))
    }

  }

  def validateTimeWithSeconds(date: LocalTime): Unit = {
    val hours = 14
    date.getHour should be(hours)
    val minutes = 2
    date.getMinute should be(minutes)
    val seconds = 20
    date.getSecond should be(seconds)
  }

  // toTime
  {
    val timeStr = "14:02Z"
    "The created Time" should "be valid and match" in {
      validateTimeDate(toTime(timeStr))
    }
    it should "throw an DateTimeParseException if the format is not valid" in {
      val timeDateStr = "14:00:00y"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '14:00:00y' could not be parsed, unparsed text found at index 8")
    }
    it should "throw an DateTimeParseException if the time is not possible" in {
      val timeDateStr = "25:00Z"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '25:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25")
    }
    it should "work even if there are tailing spaces and backslashes" in {
      validateTimeDate(toTime(" " + timeStr + "\t\n\r "))
    }
  }
  // toTimeWithoutZ
  {
    val timeStr = "14:02"
    "The created Time without Z" should "be valid and match" in {
      validateTimeDate(toTime(timeStr))
    }
    it should "throw an DateTimeParseException if the format is not valid" in {
      val timeDateStr = "_4:00"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should be("Text '_4:00' could not be parsed at index 0")
    }
    it should "throw an DateTimeParseException if the time is not possible" in {
      val timeDateStr = "25:00"
      val exc = intercept[DateTimeParseException] {
        toTime(timeDateStr)
      }
      exc.getMessage should startWith("Text '25:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25")
    }
    it should "work even if there are tailing spaces and backslashes" in {
      validateTimeDate(toTime(" " + timeStr + "\t\n\r "))
    }
  }

  def validateTimeDate(date: LocalTime): Unit = {
    val hours = 14
    date.getHour should be(hours)
    val minutes = 2
    date.getMinute should be(minutes)
  }

  // fromDayDate
  {
    "The printed Date" should "be valid and match" in {
      val dayDateStr = "2005-05-12"
      val date = toDayDate(dayDateStr)
      val result = fromDayDate(date)
      result should be(dayDateStr)
    }
    it should "throw an IllegalArgumentException if the dayDate is null" in {
      val exc = intercept[IllegalArgumentException] {
        fromDayDate(null)
      }
      exc.getMessage should be(dayMustNotBeBlank)
    }

  }
  // fromTimeDate
  {
    "The printed Time" should "be valid and match" in {
      val timeDateStr = "14:00Z"
      val date = toTime(timeDateStr)
      val result = fromTime(date)
      result should be(timeDateStr)
    }
    it should "throw an IllegalArgumentException if the timeDate is null" in {
      val exc = intercept[IllegalArgumentException] {
        fromTime(null)
      }
      exc.getMessage should be(timeMustNotBeBlank)
    }

  }
  // todayStr
  {
    object MockDateHelper extends DateTimeHelper {
      var day = 3
      override private[date] def now = {
        day += 1
        LocalDate.of(2017, 5, day)
      }
    }
    "The todayStr" should "be recalculated each time" in {
      MockDateHelper.todayStr should be("2017-05-04")
      MockDateHelper.todayStr should be("2017-05-05")
    }
    "The todayStrUTC" should "be recalculated each time" in {
      MockDateHelper.todayStrUTC should be("2017-05-05T22:00:00Z")
      MockDateHelper.todayStrUTC should be("2017-05-06T22:00:00Z")
    }
  }

}

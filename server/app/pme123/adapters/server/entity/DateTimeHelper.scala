package pme123.adapters.server.entity

import java.time._
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoField
import java.util.Date

import play.api.libs.json._
import pme123.adapters.server.entity.AdaptersContext.settings

/**
  * Created by pascal.mengelt on 09.03.2015.
  *
  */
trait DateTimeHelper {
  val dayDatePattern = "yyyy-MM-dd"
  val dayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dayDatePattern)
  val timePattern = "HH:mmZ"
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(timePattern)
  val timePatternWithoutZ = "HH:mm"
  val timeFormatterWithoutZ: DateTimeFormatter = DateTimeFormatter.ofPattern(timePatternWithoutZ)
  val timeWithSecondsPattern = "HH:mm:ssZ"
  val timeWithSecondsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(timeWithSecondsPattern)
  val timeWithSecondsPatternWithoutZ = "HH:mm:ss"
  val timeWithSecondsFormatterWithoutZ: DateTimeFormatter = DateTimeFormatter.ofPattern(timeWithSecondsPatternWithoutZ)
  val dayNotCorrectFormat = s"Date must have the format $dayDatePattern!"
  val dayMustNotBeBlank = "Date must not be blank!"
  val timeMustNotBeBlank = "Time must not be blank!"

  lazy val timezone: ZoneId = ZoneId.of(settings.timezone)

  def toDayDate(dayStr: String): LocalDate = {
    val dateLength = 10 // take only "yyyy-MM-dd"
    require(dayStr.length >= dateLength, dayNotCorrectFormat)

    LocalDate.parse(dayStr.trim.substring(0, dateLength))
  }

  def toTime(timeStr: String): LocalTime = {

    timeStr.trim match {
      case str if str.endsWith("Z") => LocalTime.parse(str.init)
      case str if str.length > timePattern.length => LocalTime.from(timeWithSecondsFormatterWithoutZ.parse(str))
      case str => LocalTime.from(timeFormatterWithoutZ.parse(str))
    }
  }

  def dayStrFromDate(day: LocalDate): String = Option(day) match {
    case Some(d) => dayDateFormatter.format(d)
    case None => throw new IllegalArgumentException(dayMustNotBeBlank)
  }

  def fromDayDate(day: LocalDate): String = Option(day) match {
    case Some(d) => dayDateFormatter.format(d)
    case None => throw new IllegalArgumentException(dayMustNotBeBlank)
  }

  def fromDayDateToUTC(day: LocalDate): String = Option(day) match {
    case Some(_) =>
      val localStartOfDay = ZonedDateTime.of(day.atStartOfDay(), timezone)
      val utcDate = localStartOfDay.withZoneSameInstant(ZoneOffset.UTC)
      DateTimeFormatter.ISO_DATE_TIME
        .format(utcDate)
    case None => throw new IllegalArgumentException(dayMustNotBeBlank)
  }

  def fromTime(time: LocalTime): String = Option(time) match {
    case Some(_) => time.format(timeFormatterWithoutZ) + "Z"
    case None => throw new IllegalArgumentException(timeMustNotBeBlank)
  }

  def fromTimeWithoutZ(time: LocalTime): String = Option(time) match {
    case Some(t) => timeFormatterWithoutZ.format(t)
    case None => throw new IllegalArgumentException(timeMustNotBeBlank)
  }

  def fromInstant(instant: Instant): String = Option(instant) match {
    case Some(i) =>
      val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(timezone)
      formatter.format(i);
    case None => throw new IllegalArgumentException(timeMustNotBeBlank)
  }

  def toDate(ldt: LocalDateTime): Date =
    Date.from(ldt.atZone(timezone).toInstant)

  def toDateFromDay(ld: LocalDate): Date =
    toDate(ld.atStartOfDay())

  def toDateFromTime(lt: LocalTime): Date =
    toDate(lt.atDate(LocalDate.now()))

  def fromDate(date: Date): LocalDateTime =
    LocalDateTime.ofInstant(date.toInstant, timezone)

  def getLocalTime(date: Date): Option[LocalTime] = {
    if (date == null)
      Option.empty
    else {
      val instant = Instant.ofEpochMilli(date.getTime).atZone(timezone)
      Option(LocalTime.of(instant.get(ChronoField.HOUR_OF_DAY), instant.get(ChronoField.MINUTE_OF_HOUR)))
    }
  }

  def getLocalDate(date: Date): Option[LocalDate] = {
    if (date == null)
      Option.empty
    else {
      val instant = Instant.ofEpochMilli(date.getTime)
      Option(LocalDateTime.ofInstant(instant, timezone).toLocalDate)
    }
  }

  def getDateFromDay(localDate: LocalDate): Date = {
    val instant = localDate.atStartOfDay().atZone(timezone).toInstant
    Date.from(instant)
  }

  // for testing
  private[entity] def now = LocalDate.now()

  def getDateFromTime(localTime: LocalTime): Date = {
    val instant = LocalDateTime.of(now, localTime).atZone(timezone).toInstant
    Date.from(instant)
  }

  def getDateFrom(localDate: LocalDate, localTime: LocalTime): Date = {
    val instant = LocalDateTime.of(localDate, localTime).atZone(timezone).toInstant
    Date.from(instant)
  }

  def localDateTimeFromInstant(instant: Instant): LocalDateTime =
    LocalDateTime.ofInstant(instant, timezone)

  def localDateTimeStrFrom(localDateTime: LocalDateTime): String =
    localDateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

  def todayStr: String = fromDayDate(now)

  def tomorrowStr: String = fromDayDate(now.plusDays(1L))

  def todayStrUTC: String = fromDayDateToUTC(now)

  def tomorrowStrUTC: String = fromDayDateToUTC(now.plusDays(1L))

  // JSON converters
  implicit val localDateReads: Reads[LocalDate] = (json: JsValue) => {
    json.validate[String]
      .map { dayStr =>
        toDayDate(dayStr)
      }
  }
  implicit val localDateWrites: Writes[LocalDate] = (localDate: LocalDate) => JsString(localDate.format(dayDateFormatter))

  implicit val localTimeReads: Reads[LocalTime] = (json: JsValue) => {
    json.validate[String]
      .map { timeStr =>
        toTime(timeStr)
      }
  }
  implicit val localTimeWrites: Writes[LocalTime] = (localTime: LocalTime) => {
    val formatted: String = localTime.format(timeFormatterWithoutZ)
    JsString(formatted)
  }
}

object DateTimeHelper extends DateTimeHelper

package pme123.adapters.server.entity

import java.time._
import java.time.format.DateTimeFormatter

import pme123.adapters.server.entity.AdaptersContext.settings

/**
  * Created by pascal.mengelt on 09.03.2015.
  *
  */
trait ISODateTimeHelper {
  val isoPattern = "yyyy-MM-dd'T'HH:mm"
  val isoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(isoPattern)

  lazy val timezone: ZoneId = ZoneId.of(settings.timezone)

  def toLocalDateTime(isoDateTimeStr: String): LocalDateTime = {

    LocalDateTime.parse(isoDateTimeStr, isoFormatter)
  }

  def toISODateTimeString(dateTime: LocalDateTime): String =
    isoFormatter.format(dateTime)
}


object ISODateTimeHelper extends ISODateTimeHelper
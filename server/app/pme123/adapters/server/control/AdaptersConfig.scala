package pme123.adapters.server.control

import java.time._
import javax.inject.Inject

import com.typesafe.config.Config
import play.api.Configuration
import pme123.adapters.server.entity.{DateTimeHelper, SmtpConfig}
import pme123.adapters.shared.LogLevel

import scala.collection.JavaConverters._
class AdaptersConfig @Inject()(config: Configuration) {
  import AdaptersConfig._

  val aConfig: Config = config.get[Config](configPath)

  def createUrl(host: String, port: Int, sslEnabled: Boolean): String = {
    val protocol = if (sslEnabled) "https" else "http"
    s"$protocol://$host:$port"
  }

  // note that these fields are NOT lazy, because if we're going to
  // get any exceptions, we want to get them on startup.
  val httpContext: String = config.get[String](httpContextProp)
  val project: String = aConfig.getString(projectProp)
  val runMode: String = aConfig.getString(runModeProp)
  val timezone: String = aConfig.getString(timezoneProp)

  // mail server
  val mailSmtpTls: Boolean = aConfig.getBoolean(mailSmtpTlsProp)
  val mailSmtpSsl: Boolean = aConfig.getBoolean(mailSmtpSslProp)
  val mailHost: String = aConfig.getString(mailHostProp)
  val mailPort: Int = aConfig.getInt(mailPortProp)
  val mailUsername: String = aConfig.getString(mailUsernameProp)
  val mailPassword: String = aConfig.getString(mailPasswordProp)
  val smtpConfig = SmtpConfig(
    mailSmtpTls,
    mailSmtpSsl,
    mailPort,
    mailHost,
    mailUsername,
    mailPassword
  )
  val mailFrom: String = aConfig.getString(mailFromProp)
  val adminMailActive: Boolean = aConfig.getBoolean(adminMailActiveProp)
  val adminMailRecipient: String = aConfig.getString(adminMailRecipientProp)
  val adminMailSubject: String = aConfig.getString(adminMailSubjectProp)
  val adminMailLoglevel: LogLevel = LogLevel.fromLevel(aConfig.getString(adminMailLoglevelProp)).get
  val schedulerExecutionFirstWeekday: String = aConfig.getString(schedulerExecutionFirstWeekdayProp)
  val profilesReplaceMissing: Boolean = aConfig.getBoolean(profilesReplaceMissingProp)
  val profilesAllowEmptyValues: Boolean = aConfig.getBoolean(profilesAllowEmptyValuesProp)
  val wsocketHostsAllowed: Seq[String] = aConfig.getStringList(wsocketHostsAllowedProp).asScala
  // for testing
  private[control] def dayOfWeek: Option[DayOfWeek] = schedulerExecutionFirstWeekday match {
    case "monday" => Some(DayOfWeek.MONDAY)
    case "tuesday" => Some(DayOfWeek.TUESDAY)
    case "wednesday" => Some(DayOfWeek.WEDNESDAY)
    case "thursday" => Some(DayOfWeek.THURSDAY)
    case "friday" => Some(DayOfWeek.FRIDAY)
    case "saturday" => Some(DayOfWeek.SATURDAY)
    case "sunday" => Some(DayOfWeek.SUNDAY)
    case _ => None
  }

  // for testing
  private[control] def now = LocalDate.now

  private val firstTime = {
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
      .of(now, DateTimeHelper.toTime(aConfig.getString(schedulerExecutionFirstTimeProp)))
      .plusDays(offset)
    val zo = ZoneId.of(timezone).getRules.getOffset(ldt)
    ldt.toInstant(zo)
  }

  val schedulerExecutionFirstTime: Instant = firstTime
  val schedulerExecutionPeriodInMin: Int = aConfig.getInt(schedulerExecutionPeriodInMinProp)
  val importLogEnabled: Boolean = aConfig.getBoolean(importLogEnabledProp)
  val importLogPath: String = aConfig.getString(importLogPathProp)
  val logService: String = aConfig.getString(logServiceProp)

  lazy val isProdMode: Boolean = runMode == "PROD"
  lazy val isDevMode: Boolean = runMode == "DEV"


}

object AdaptersConfig {

  val configPath = "pme123.adapters.server"
  val httpContextProp = "play.http.context"
  val projectProp = "project"
  val runModeProp = "run.mode"
  val timezoneProp = "timezone"

  val mailHostProp = "mail.host"
  val mailPortProp = "mail.port"
  val mailSmtpTlsProp = "mail.smtp.tls"
  val mailSmtpSslProp = "mail.smtp.ssl"
  val mailUsernameProp = "mail.username"
  val mailPasswordProp = "mail.password"
  val mailFromProp = "mail.from"
  val adminMailActiveProp = "admin.mail.active"
  val adminMailRecipientProp = "admin.mail.recipient"
  val adminMailSubjectProp = "admin.mail.subject"
  val adminMailLoglevelProp = "admin.mail.loglevel"
  val schedulerExecutionFirstTimeProp = "scheduler.execution.first.time"
  val schedulerExecutionFirstWeekdayProp = "scheduler.execution.first.weekday"
  val schedulerExecutionPeriodInMinProp = "scheduler.execution.period.minutes"
  val importLogEnabledProp = "import.log.enabled"
  val importLogPathProp = "import.log.path"
  val logServiceProp = "log.service"
  val profilesReplaceMissingProp = "profiles.replace.missing"
  val profilesAllowEmptyValuesProp = "profiles.allow.empty.values"
  val wsocketHostsAllowedProp = "wsocket.hosts.allowed"

}

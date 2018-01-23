package pme123.adapters.server.entity

import java.time._

import com.typesafe.config.{Config, ConfigFactory}
import pme123.adapters.server.entity.AdaptersSettings.wsocketHostsAllowedProp
import pme123.adapters.shared._

import scala.collection.JavaConverters._
import scala.language.implicitConversions
/**
  * created by pascal.mengelt
  * This config uses the small framework typesafe-config.
  * See here the explanation: https://github.com/typesafehub/config
  */
object AdaptersSettings extends Logger {
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

  def config(): Config = {
    ConfigFactory.invalidateCaches()
    ConfigFactory.load()
  }
}

// this settings will be validated on startup
class AdaptersSettings(config: Config) extends Logger {
  import AdaptersSettings._

  // checkValid(), just as in the plain SimpleLibContext
  config.checkValid(ConfigFactory.defaultReference(), configPath)

  def createUrl(host: String, port: Int, sslEnabled: Boolean): String = {
    val protocol = if (sslEnabled) "https" else "http"
    s"$protocol://$host:$port"
  }

  val projectConfig: Config = config.getConfig(configPath)

  // note that these fields are NOT lazy, because if we're going to
  // get any exceptions, we want to get them on startup.
  val httpContext: String = config.getString(httpContextProp)
  val project: String = projectConfig.getString(projectProp)
  val runMode: String = projectConfig.getString(runModeProp)
  val timezone: String = projectConfig.getString(timezoneProp)

  // mail server
  val mailSmtpTls: Boolean = projectConfig.getBoolean(mailSmtpTlsProp)
  val mailSmtpSsl: Boolean = projectConfig.getBoolean(mailSmtpSslProp)
  val mailHost: String = projectConfig.getString(mailHostProp)
  val mailPort: Int = projectConfig.getInt(mailPortProp)
  val mailUsername: String = projectConfig.getString(mailUsernameProp)
  val mailPassword: String = projectConfig.getString(mailPasswordProp)
  val smtpConfig = SmtpConfig(
    mailSmtpTls,
    mailSmtpSsl,
    mailPort,
    mailHost,
    mailUsername,
    mailPassword
  )
  val mailFrom: String = projectConfig.getString(mailFromProp)
  val adminMailActive: Boolean = projectConfig.getBoolean(adminMailActiveProp)
  val adminMailRecipient: String = projectConfig.getString(adminMailRecipientProp)
  val adminMailSubject: String = projectConfig.getString(adminMailSubjectProp)
  val adminMailLoglevel: LogLevel = LogLevel.fromLevel(projectConfig.getString(adminMailLoglevelProp)).get
  val schedulerExecutionFirstWeekday: String = projectConfig.getString(schedulerExecutionFirstWeekdayProp)
  val profilesReplaceMissing: Boolean = projectConfig.getBoolean(profilesReplaceMissingProp)
  val profilesAllowEmptyValues: Boolean = projectConfig.getBoolean(profilesAllowEmptyValuesProp)
  val wsocketHostsAllowed: Seq[String] = projectConfig.getStringList(wsocketHostsAllowedProp).asScala
  // for testing
  private[entity] def dayOfWeek: Option[DayOfWeek] = schedulerExecutionFirstWeekday match {
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
  private[entity] def now = LocalDate.now

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
      .of(now, DateTimeHelper.toTime(projectConfig.getString(schedulerExecutionFirstTimeProp)))
      .plusDays(offset)
    val zo = ZoneId.of(timezone).getRules.getOffset(ldt)
    ldt.toInstant(zo)
  }

  val schedulerExecutionFirstTime: Instant = firstTime
  val schedulerExecutionPeriodInMin: Int = projectConfig.getInt(schedulerExecutionPeriodInMinProp)
  val importLogEnabled: Boolean = projectConfig.getBoolean(importLogEnabledProp)
  val importLogPath: String = projectConfig.getString(importLogPathProp)
  val logService: String = projectConfig.getString(logServiceProp)

  lazy val isProdMode: Boolean = runMode == "PROD"
  lazy val isDevMode: Boolean = runMode == "DEV"
}

import pme123.adapters.server.entity.AdaptersSettings._

// This is a different way to do AdaptersContext, using the
// AdaptersSettings class to encapsulate and validate the
// settings on startup
class AdaptersContext(config: Config)
  extends AdaptersContextPropsImplicits {

  val name = "common"
  lazy val settings = new AdaptersSettings(config)

  lazy val props: Seq[AdaptersContextProp] =
    Seq(
      AdaptersContextProp(httpContextProp, settings.httpContext)
      , AdaptersContextProp(projectProp, settings.project)
      , AdaptersContextProp(runModeProp, settings.runMode)
      , AdaptersContextProp(timezoneProp, settings.timezone)
      , AdaptersContextProp(mailSmtpTlsProp, settings.mailSmtpTls)
      , AdaptersContextProp(mailSmtpSslProp, settings.mailSmtpSsl)
      , AdaptersContextProp(mailPortProp, settings.mailPort)
      , AdaptersContextProp(mailHostProp, settings.mailHost)
      , AdaptersContextProp(mailUsernameProp, settings.mailUsername)
      , AdaptersContextProp(mailPasswordProp, pwd(settings.mailPassword))
      , AdaptersContextProp(mailFromProp, settings.mailFrom)
      , AdaptersContextProp(adminMailActiveProp, settings.adminMailActive)
      , AdaptersContextProp(adminMailRecipientProp, settings.adminMailRecipient)
      , AdaptersContextProp(adminMailSubjectProp, settings.adminMailSubject)
      , AdaptersContextProp(adminMailLoglevelProp, settings.adminMailLoglevel)
      , AdaptersContextProp(schedulerExecutionFirstTimeProp, settings.schedulerExecutionFirstTime)
      , AdaptersContextProp(schedulerExecutionFirstWeekdayProp, settings.schedulerExecutionFirstWeekday)
      , AdaptersContextProp(schedulerExecutionPeriodInMinProp, settings.schedulerExecutionPeriodInMin)
      , AdaptersContextProp(importLogEnabledProp, settings.importLogEnabled)
      , AdaptersContextProp(importLogPathProp, settings.importLogPath)
      , AdaptersContextProp(profilesReplaceMissingProp, settings.profilesReplaceMissing)
      , AdaptersContextProp(profilesAllowEmptyValuesProp, settings.profilesAllowEmptyValues)
      , AdaptersContextProp(wsocketHostsAllowedProp, settings.wsocketHostsAllowed)
    )
}

// default Configuration
object AdaptersContext extends AdaptersContext(config()) {
  AdaptersContext.logSettings
}

// this contains implicit conversions!
trait AdaptersContextPropsImplicits
  extends Logger {
  def name: String

  def props: Seq[AdaptersContextProp]

  lazy val logSettings: Seq[LogEntry] =
    props.map(printString)
      .map(info(_))

  lazy val asString: String = props.map(printString).mkString("\n")

  lazy val asHtml: String = "<LI>" + props.map(printString)
    .mkString("</LI><LI>") + "</LI>"

  protected def pwd(value:String): String = "*" * value.length

  private def printString(prop: AdaptersContextProp): String =
    propString(prop.key, prop.value)

  private def propString(label: String, value: Any) =
    s"${name.toUpperCase} '$label' >> $value"


  implicit def fromAny(bool: Boolean): String = bool.toString

  implicit def fromInt(nbr: Int): String = nbr.toString

  implicit def fromLogLevel(ll: LogLevel): String = ll.toString

  implicit def fromInstant(inst: Instant): String = DateTimeHelper.fromInstant(inst)

  implicit def fromSeq(seq: Seq[String]): String = seq.mkString("[", ",", "]")

}


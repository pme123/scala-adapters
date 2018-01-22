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
  val projectProp = s"$configPath.project"
  val runModeProp = s"$configPath.run.mode"
  val timezoneProp = s"$configPath.timezone"

  val mailHostProp = s"$configPath.mail.host"
  val mailPortProp = s"$configPath.mail.port"
  val mailSmtpTlsProp = s"$configPath.mail.smtp.tls"
  val mailSmtpSslProp = s"$configPath.mail.smtp.ssl"
  val mailUsernameProp = s"$configPath.mail.username"
  val mailPasswordProp = s"$configPath.mail.password"
  val mailFromProp = s"$configPath.mail.from"
  val adminMailActiveProp = s"$configPath.admin.mail.active"
  val adminMailRecipientProp = s"$configPath.admin.mail.recipient"
  val adminMailSubjectProp = s"$configPath.admin.mail.subject"
  val adminMailLoglevelProp = s"$configPath.admin.mail.loglevel"
  val schedulerExecutionFirstTimeProp = s"$configPath.scheduler.execution.first.time"
  val schedulerExecutionFirstWeekdayProp = s"$configPath.scheduler.execution.first.weekday"
  val schedulerExecutionPeriodInMinProp = s"$configPath.scheduler.execution.period.minutes"
  val importLogEnabledProp = s"$configPath.import.log.enabled"
  val importLogPathProp = s"$configPath.import.log.path"
  val logServiceProp = s"$configPath.log.service"
  val profilesReplaceMissingProp = s"$configPath.profiles.replace.missing"
  val profilesAllowEmptyValuesProp = s"$configPath.profiles.allow.empty.values"
  val wsocketHostsAllowedProp = s"$configPath.wsocket.hosts.allowed"

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

  // note that these fields are NOT lazy, because if we're going to
  // get any exceptions, we want to get them on startup.
  val httpContext: String = config.getString(httpContextProp)
  val project: String = config.getString(projectProp)
  val runMode: String = config.getString(runModeProp)
  val timezone: String = config.getString(timezoneProp)

  // mail server
  val mailSmtpTls: Boolean = config.getBoolean(mailSmtpTlsProp)
  val mailSmtpSsl: Boolean = config.getBoolean(mailSmtpSslProp)
  val mailHost: String = config.getString(mailHostProp)
  val mailPort: Int = config.getInt(mailPortProp)
  val mailUsername: String = config.getString(mailUsernameProp)
  val mailPassword: String = config.getString(mailPasswordProp)
  val smtpConfig = SmtpConfig(
    mailSmtpTls,
    mailSmtpSsl,
    mailPort,
    mailHost,
    mailUsername,
    mailPassword
  )
  val mailFrom: String = config.getString(mailFromProp)
  val adminMailActive: Boolean = config.getBoolean(adminMailActiveProp)
  val adminMailRecipient: String = config.getString(adminMailRecipientProp)
  val adminMailSubject: String = config.getString(adminMailSubjectProp)
  val adminMailLoglevel: LogLevel = LogLevel.fromLevel(config.getString(adminMailLoglevelProp)).get
  val schedulerExecutionFirstWeekday: String = config.getString(schedulerExecutionFirstWeekdayProp)
  val profilesReplaceMissing: Boolean = config.getBoolean(profilesReplaceMissingProp)
  val profilesAllowEmptyValues: Boolean = config.getBoolean(profilesAllowEmptyValuesProp)
  val wsocketHostsAllowed: Seq[String] = config.getStringList(wsocketHostsAllowedProp).asScala
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
      .of(now, DateTimeHelper.toTime(config.getString(schedulerExecutionFirstTimeProp)))
      .plusDays(offset)
    val zo = ZoneId.of(timezone).getRules.getOffset(ldt)
    ldt.toInstant(zo)
  }

  val schedulerExecutionFirstTime: Instant = firstTime
  val schedulerExecutionPeriodInMin: Int = config.getInt(schedulerExecutionPeriodInMinProp)
  val importLogEnabled: Boolean = config.getBoolean(importLogEnabledProp)
  val importLogPath: String = config.getString(importLogPathProp)
  val logService: String = config.getString(logServiceProp)

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


package pme123.adapters.server.entity

import java.time._

import com.typesafe.config.{Config, ConfigFactory}
import pme123.adapters.server.entity.AdaptersSettings.wsocketHostsAllowedProp
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared._

import scala.collection.JavaConverters._
import scala.language.implicitConversions
/**
  * created by pascal.mengelt
  * This config uses the small framework typesafe-config.
  * See here the explanation: https://github.com/typesafehub/config
  */
object AdaptersSettings extends Logger {
  val configPath = "pme123.adapters"

  val httpContextProp = "play.http.context"
  val runModeProp = "run.mode"
  val charEncodingProp = "char.encoding"
  val timezoneProp = "timezone"

  val mailHostProp = "mail.host"
  val mailPortProp = "mail.port"
  val mailSmtpAuthProp = "mail.smtp.auth"
  val mailSmtpTlsProp = "mail.smtp.tls"
  val mailSmtpSslProp = "mail.smtp.ssl"
  val mailUsernameProp = "mail.username"
  val mailPasswordProp = "mail.password"
  val mailFromProp = "mail.from"
  val adminMailActiveProp = "admin.mail.active"
  val adminMailRecipientProp = "admin.mail.recipient"
  val adminMailSubjectProp = "admin.mail.subject"
  val adminMailLoglevelProp = "admin.mail.loglevel"
  val processLogEnabledProp = "process.log.enabled"
  val processLogPathProp = "process.log.path"
  val wsocketHostsAllowedProp = "wsocket.hosts.allowed"

  val projectConfigProp = "project.config"
  val jobConfigsProp = "job.configs"

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

  val baseConfig: Config = config.getConfig(configPath)

  // note that these fields are NOT lazy, because if we're going to
  // get any exceptions, we want to get them on startup.
  val httpContext: String = config.getString(httpContextProp)
  val runMode: String = baseConfig.getString(runModeProp)
  val charEncoding: String = baseConfig.getString(charEncodingProp)
  val timezone: String = baseConfig.getString(timezoneProp)
  val timezoneID: ZoneId = ZoneId.of(timezone)

  // mail server
  val mailSmtpTls: Boolean = baseConfig.getBoolean(mailSmtpTlsProp)
  val mailSmtpSsl: Boolean = baseConfig.getBoolean(mailSmtpSslProp)
  val mailSmtpAuth: Boolean = baseConfig.getBoolean(mailSmtpAuthProp)
  val mailHost: String = baseConfig.getString(mailHostProp)
  val mailPort: Int = baseConfig.getInt(mailPortProp)
  val mailUsername: String = baseConfig.getString(mailUsernameProp)
  val mailPassword: String = baseConfig.getString(mailPasswordProp)
  val smtpConfig = SmtpConfig(
    mailSmtpTls,
    mailSmtpSsl,
    mailPort,
    mailSmtpAuth,
    mailHost,
    mailUsername,
    mailPassword
  )
  val mailFrom: String = baseConfig.getString(mailFromProp)
  val adminMailActive: Boolean = baseConfig.getBoolean(adminMailActiveProp)
  val adminMailRecipient: String = baseConfig.getString(adminMailRecipientProp)
  val adminMailSubject: String = baseConfig.getString(adminMailSubjectProp)
  val adminMailLoglevel: LogLevel = LogLevel.fromLevel(baseConfig.getString(adminMailLoglevelProp)).get
  val wsocketHostsAllowed: Seq[String] = baseConfig.getStringList(wsocketHostsAllowedProp).asScala

  val processLogEnabled: Boolean = baseConfig.getBoolean(processLogEnabledProp)
  val processLogPath: String = baseConfig.getString(processLogPathProp)

  val isProdMode: Boolean = runMode == "PROD"
  val isDevMode: Boolean = runMode == "DEV"

  val projectConfig: Config = baseConfig.getConfig(projectConfigProp)

  val jobConfigs: Map[JobIdent, JobConfig] =
    baseConfig.getConfigList(jobConfigsProp).asScala
      .map { c =>
      JobConfigCreator(c, ZoneId.of(timezone)).create()
    }.toMap

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
      , AdaptersContextProp(runModeProp, settings.runMode)
      , AdaptersContextProp(timezoneProp, settings.timezone)
      , AdaptersContextProp(charEncodingProp, settings.charEncoding)
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
      , AdaptersContextProp(projectConfigProp, settings.projectConfig.toString)
      , AdaptersContextProp(jobConfigsProp, settings.jobConfigs.toString)
      , AdaptersContextProp(processLogEnabledProp, settings.processLogEnabled)
      , AdaptersContextProp(processLogPathProp, settings.processLogPath)
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


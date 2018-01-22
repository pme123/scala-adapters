package pme123.adapters.server.control.mail

import java.time.Instant

import pme123.adapters.server.entity
import pme123.adapters.shared.{LogLevel, Logger}
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.{MailAttachment, MailMessage}
import pme123.adapters.shared.LogReport

import scala.concurrent.duration._

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  */
trait MailNotifier extends Logger {
  lazy val mailService: MailService = MailService

  def notifyAdmin(recipient: Option[String], logReport: LogReport, attachment: Option[MailAttachment] = None) {
    if (adminMailActive) {
      createMail(recipient, logReport, adminMailLoglevel, attachment).foreach(mailService.send)
    } else {
      info("Mail notification is NOT active!")
    }
  }

  def createMail(recipient: Option[String], logReport: LogReport, logLevel: LogLevel, attachment: Option[MailAttachment] = None): Option[MailMessage] = {
    val print = logReport.createPrint(logLevel)

    if (print.trim.isEmpty) {
      None
    } else {
      Some(entity.MailMessage(
        s"${logReport.maxLevel().level.toUpperCase}: $adminMailSubject [${Instant.now}]",
        recipient.getOrElse(adminMailRecipient),
        mailFrom,
        print,
        smtpConfig,
        1.minute,
        attachment = attachment
      ))
    }

  }
}

object MailNotifier extends MailNotifier

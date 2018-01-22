package pme123.adapters.server.entity

import scala.concurrent.duration._

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  * The email message sent to Actors in charge of delivering email
  *
  * @param subject   the email subject
  * @param recipient the recipient
  * @param from      the sender
  * @param text      alternative simple text
  */
case class MailMessage(subject: String
                       , recipient: String
                       , from: String
                       , text: String
                       , smtpConfig: SmtpConfig
                       , retryOn: FiniteDuration
                       , var deliveryAttempts: Int = 0
                       , attachment: Option[MailAttachment] = None
                      )

case class MailAttachment(name: String, path: String)

/**
  * Smtp config
  *
  * @param tls      if tls should be used with the smtp connections
  * @param ssl      if ssl should be used with the smtp connections
  * @param port     the smtp port
  * @param host     the smtp host name
  * @param user     the smtp user
  * @param password thw smtp password
  */
case class SmtpConfig(
                       tls: Boolean = false,
                       ssl: Boolean = false,
                       port: Int = 25,
                       host: String,
                       user: String,
                       password: String
                     )

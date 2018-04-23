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
  * @param smtpAuth true if there is an authentication needed
  */
case class SmtpConfig(
                       tls: Boolean = false
                       , ssl: Boolean = false
                       , port: Int = 25
                       , smtpAuth: Boolean = false
                       , host: String
                       , user: String
                       , password: String
                     )

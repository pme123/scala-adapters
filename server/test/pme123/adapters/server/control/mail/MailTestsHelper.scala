package pme123.adapters.server.control.mail

import pme123.adapters.server.entity.{MailMessage, SmtpConfig}

import scala.concurrent.duration._
/**
 * Created by pascal.mengelt on 16.03.2015.
 *
 */
trait MailTestsHelper {
  val port = 2500
  val subject = "Unit Test Subject"
  val content = "this is the Unit Test message"
  val sender = "pme123@example.com"
  val recipient = "receiver@example.com"

  val smtpConfig = SmtpConfig(host = "localhost", port=port, user = "pme123.adapters", password = "pwd1234")
  val message = MailMessage(
    subject,
    recipient,
    sender,
    content,
    smtpConfig,
    10.minutes
  )
}

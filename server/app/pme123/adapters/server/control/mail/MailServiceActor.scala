package pme123.adapters.server.control.mail

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, OneForOneStrategy, Props}
import org.apache.commons.mail._
import pme123.adapters.server.entity.{MailAttachment, MailMessage}
import pme123.adapters.shared.Logger

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  * An Email sender actor that sends out email messages
  * Retries delivery up to 10 times every 10 minutes as long as it receives
  * an EmailException, gives up at any other type of exception
  */
class MailServiceActor extends Actor with Logger {
  val maxMailRetries = 10
  /**
    * The actor supervisor strategy attempts to send email up to 10 times if there is a EmailException
    */
  override val supervisorStrategy: OneForOneStrategy =
  OneForOneStrategy(maxNrOfRetries = maxMailRetries) {
    case emailException: EmailException =>
      warn(s"Restarting after receiving EmailException : ${emailException.getMessage}")
      Restart
    case unknownException: Exception =>
      error(s"Giving up. Can you recover from this? : $unknownException")
      Stop
    case unknownCase: Any =>
      error(s"Giving up on unexpected case : $unknownCase")
      Stop
  }

  /**
    * Forwards messages to child workers
    */
  def receive: PartialFunction[Any, Unit] = {
    case message: Any => context.actorOf(Props[MailServiceWorker]) ! message
  }

}

object MailServiceWorker {
  val receivedUnexpectedMsg = "Received unexpected message that can't be handled: "
}

/**
  * Email worker that delivers the message
  */
class MailServiceWorker extends Actor with Logger {

  import MailServiceWorker.receivedUnexpectedMsg

  /**
    * The email message in scope
    */
  private var mailMessage: Option[MailMessage] = None

  /**
    * Delivers a message
    */
  def receive: PartialFunction[Any, Unit] = {
    case mail: MailMessage =>
      mailMessage = Option(mail)
      mail.deliveryAttempts = mail.deliveryAttempts + 1
      info(s"Attempting to deliver message (${mail.recipient})")
      sendEmailSync(mail)
      info("Message delivered")
    case unexpectedMessage: Any =>
      sender ! error(receivedUnexpectedMsg + unexpectedMessage)
  }

  /**
    * Private helper invoked by the actors that sends the email
    */
  private def sendEmailSync(mailMessage: MailMessage) {

    // Create the mail message
    val mail = mailMessage.attachment.map(attachEmail).getOrElse(new SimpleEmail())
    mail.setStartTLSEnabled(mailMessage.smtpConfig.tls)
    mail.setSSLOnConnect(mailMessage.smtpConfig.ssl)
    mail.setSmtpPort(mailMessage.smtpConfig.port)
    mail.setHostName(mailMessage.smtpConfig.host)
    if (null != mailMessage.smtpConfig.user) {
      mail.setAuthenticator(new DefaultAuthenticator(
        mailMessage.smtpConfig.user,
        mailMessage.smtpConfig.password
      ))
    }
    mail.setMsg(mailMessage.text)
      .addTo(mailMessage.recipient)
      .setFrom(mailMessage.from)
      .setSubject(mailMessage.subject)
      .send()
  }

  private def attachEmail(mailAttachment: MailAttachment) = {
    val attachment = new EmailAttachment()
    attachment.setPath(mailAttachment.path)
    attachment.setDisposition(EmailAttachment.ATTACHMENT)
    attachment.setName(mailAttachment.name)
    new MultiPartEmail().attach(attachment)
  }

  /**
    * If this child has been restarted due to an exception attempt redelivery
    * based on the message configured delay
    */
  override def preRestart(reason: Throwable, message: Option[Any]) {
    if (mailMessage.isDefined) {
      info(s"Scheduling email message to be sent after attempts: ${mailMessage.get}")
      import context.dispatcher
      // Use this Actors' Dispatcher as ExecutionContext

      context.system.scheduler.scheduleOnce(mailMessage.get.retryOn, self, mailMessage.get)
    }
  }

  override def postStop() {
    if (mailMessage.isDefined) {
      info(s"Stopped child email worker after attempts ${mailMessage.get.deliveryAttempts}, $self")
    }
  }

}

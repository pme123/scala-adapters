package pme123.adapters.server.control.mail

import org.scalamock.scalatest.MockFactory
import pme123.adapters.server.entity.mail.MailAttachment
import pme123.adapters.shared.LogLevel._
import pme123.adapters.shared._
import pme123.adapters.server.entity.AdaptersContext.settings.smtpConfig
import pme123.adapters.server.entity.{MailAttachment, MailMessage, UnitTest}
import pme123.adapters.shared.LogReport

import scala.concurrent.duration._

/**
  * Created by pascal.mengelt on 17.03.2015.
  *
  */
class MailNotifierTest
  extends UnitTest
    with MockFactory {

  // Notify the admin is ignored because of problem with the Mocking framework (Nullpointer: https://github.com/paulbutcher/ScalaMock/issues/25)
  ignore should "create a MailMessage and call the MailService" in {
    val mailService = mock[MailService]
    val mailMessage = mock[MockedMailMessage]
    (mailService.send _).expects(mailMessage)
    new MockedMailService(mailService, mailMessage).notifyAdmin(None, new LogReport("MailNotifierTest"))
  }
  {
    val logReport = new LogReport("MailNotifierTest")
    val infoEntry = LogEntry(INFO, "blabla")
    logReport + infoEntry
    s"Create a MailMessage " should "return a MailMessage" in {
      val mailOption = MailNotifier.createMail(None, logReport, INFO)
      assert(mailOption.get.text.contains(infoEntry.msg))
    }
    it should "should return nothing if all LogEntries are filtered by the LogLevel." in {
      val mailOption = MailNotifier.createMail(None, logReport, WARN)
      mailOption should be(None)
    }
    it should "should contain a custom recipient if specified." in {
      val recipient = "supo@dupi.ch"
      val mailOption = MailNotifier.createMail(Some(recipient), logReport, INFO)
      mailOption.get.recipient should be(recipient)
    }
    it should "should contain an attachment." in {
      val attachName = "attach.gif"
      val attachPath = "/temp/123attach.gif"
      val mailOption = MailNotifier.createMail(Some("supi@dupi.ch"), logReport, INFO, Some(MailAttachment(attachName, attachPath)))
      mailOption.get.attachment.get.name should be(attachName)
      mailOption.get.attachment.get.path should be(attachPath)
    }
  }
}

class MockedMailService(_mailService: MailService, mailMessage: MailMessage)
  extends MailNotifier {
  override lazy val mailService: MailService = _mailService

  override def createMail(recipient: Option[String], logReport: LogReport, logLevel: LogLevel, attachment: Option[MailAttachment] = None): Option[MailMessage] = Some(mailMessage)
}

class MockedMailMessage extends MailMessage("subject", "recipient", "from", "text", smtpConfig, 2.seconds)

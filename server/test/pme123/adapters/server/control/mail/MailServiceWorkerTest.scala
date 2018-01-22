package pme123.adapters.server.control.mail

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest._
import org.subethamail.wiser.Wiser
import pme123.adapters.server.control.mail.MailServiceWorker.receivedUnexpectedMsg
import pme123.adapters.shared.LogEntry
import pme123.adapters.shared.LogLevel.ERROR

import scala.concurrent.duration._

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  */
class MailServiceWorkerTest(_system: ActorSystem) extends TestKit(_system)
  with MailTestsHelper
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private implicit val timeout: Timeout = new Timeout(10.seconds)

  def this() = this(ActorSystem("MailServiceWorkerTest"))


  val wiser = new Wiser()

  override def beforeAll() {
    wiser.setPort(port)
    wiser.start()
  }

  override def afterAll() {
    wiser.stop()
    TestKit.shutdownActorSystem(system)
  }

  "A MailServiceWorker" must {
    "send the message" in {
      val actorRef = TestActorRef(new MailServiceWorker)
      val future = actorRef ? message
      future.value

      wiser.getMessages.size should be(1)
      val resultMsg = wiser.getMessages.get(0)
      resultMsg.getEnvelopeSender should be(sender)
      resultMsg.getEnvelopeReceiver should be(recipient)
      val mimeMsg = resultMsg.getMimeMessage
      mimeMsg.getContent.toString.trim should be(content)
      mimeMsg.getSubject should be(subject)
    }
  }


  "A MailServiceWorker" must {
    "log an Error and return a LogEntry" in {
      val actorRef = TestActorRef(new MailServiceWorker)
      val badMsg = "bad Message"
      val future = actorRef ? badMsg
      val tryLogEntry = future.value.get
      val logEntry = tryLogEntry.get.asInstanceOf[LogEntry]
      logEntry.level should be(ERROR)
      logEntry.msg should be(s"$receivedUnexpectedMsg$badMsg")
    }
  }

}


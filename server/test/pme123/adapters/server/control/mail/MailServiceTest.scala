package pme123.adapters.server.control.mail

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import pme123.adapters.server.entity.UnitTest

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  */
class MailServiceTest
  extends UnitTest
    with MailTestsHelper {

  val system = ActorSystem("testSystem")

  after {
    TestKit.shutdownActorSystem(system)
  }

  "A MailService" should "send messages" in {
    // Just run - no exception should be thrown
    TestMailService(system).send(message)
  }


}

case class TestMailService(_system: ActorSystem) extends TestKit(_system) with MailService {
  override lazy val mailServiceActor: TestActorRef[MailServiceActor] = TestActorRef(new MailServiceActor)
}

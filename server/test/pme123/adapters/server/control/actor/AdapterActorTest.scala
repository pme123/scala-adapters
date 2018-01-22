package pme123.adapters.server.control.actor

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import pme123.adapters.shared.RunAdapter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pascal.mengelt on 23.04.2015.
  *
  */
class AdapterActorTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def this() = this(ActorSystem("ImportActorTest"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An ImportActor" must {
    val actorRef = system.actorOf(Props(TestAdapterActor()))
    "return nothing after sending a RunImport" in {
      actorRef ! RunAdapter("user")
      expectNoMessage(2.seconds)
    }
  }

}

case class TestAdapterActor()(implicit val mat: Materializer, val ec: ExecutionContext)
  extends AdapterActor {

  override protected def runAdapter(user: String): Future[Unit] = Future{
    assert("user" == user)
  }
}






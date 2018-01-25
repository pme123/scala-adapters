package pme123.adapters.server.control.actor

import java.time.Instant

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import pme123.adapters.server.entity.TestResources
import pme123.adapters.shared.{JobConfig, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by pascal.mengelt on 13.03.2015.
  *
  */
class AdapterSchedulerTest(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with Logger {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def this() = this(ActorSystem("ImportSchedulerTest"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val scheduler = TestAdapterScheduler()
  "A ImportScheduler" must {
    "init correctly" in {
      val executionPeriod = 1.day.toMinutes
      scheduler.intervalDuration.length should be(executionPeriod * 60)
      assert(scheduler.firstExecution isAfter Instant.now)
      assert((scheduler.firstExecution.toEpochMilli - Instant.now.toEpochMilli) <= scheduler.intervalDuration.toMillis)
      debug(s"The first import is ${scheduler.firstExecution}")
    }
  }

  it must {
    "init correctly with offset" in {
      val offsetInMin = 60
      val scheduler2 = TestAdapterScheduler(offsetInMin)
      scheduler2.firstExecution should be(scheduler.firstExecution.plusSeconds(offsetInMin * 60))
      debug(s"The first import is ${scheduler2.firstExecution}")
    }
  }

}

case class TestAdapterScheduler(offsetInMin: Int = 0)
                               (implicit val mat: Materializer, val actorSystem: ActorSystem, val ec: ExecutionContext)
  extends AdapterScheduler
    with TestResources {
  val actorRef = TestActorRef(TestAdapterActor())
  val adapterActor: ActorRef = actorRef


  override def jobConfig: JobConfig = jobConfigDefault
}


package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl._
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.JsValue
import pme123.adapters.server.control.UserActor.CreateAdapter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
/**
 * Provide some DI and configuration sugar for new UserActor instances.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
class UserParentActor @Inject()(childFactory: UserActor.Factory,
                                configuration: Configuration)
                               (implicit ec: ExecutionContext)
  extends Actor with InjectedActorSupport with ActorLogging {

  import UserParentActor._

  implicit private val timeout: Timeout = Timeout(2.seconds)

  override def receive: Receive = LoggingReceive {
    case Create(id, adapterActor) =>
      val name = s"userActor-$id"
      log.info(s"Creating initiator actor $name")
      val child: ActorRef = injectedChild(childFactory(id, adapterActor), name)
      val future = (child ? CreateAdapter(id)).mapTo[Flow[JsValue, JsValue, _]]
      pipe(future) to sender()
  }
}

object UserParentActor {
  case class Create(id: String, adapterActor: ActorRef)
}

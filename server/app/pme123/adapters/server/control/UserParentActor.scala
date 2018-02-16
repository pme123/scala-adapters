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

  import pme123.adapters.server.entity.ActorMessages._

  implicit private val timeout: Timeout = Timeout(2.seconds)

  override def receive: Receive = LoggingReceive {
    case Create(clientConfig, jobActor) =>
      val name = s"userActor-${clientConfig.requestIdent}"
      log.info(s"Creating initiator actor $name")
      val child: ActorRef = injectedChild(childFactory(clientConfig, jobActor), name)
      val future = (child ? InitActor).mapTo[Flow[JsValue, JsValue, _]]
      pipe(future) to sender()
  }
}


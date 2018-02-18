package pme123.adapters.server.control

import javax.inject.{Inject, Named}

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl._
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.JsValue
import pme123.adapters.server.control.ClientActor.GetClientConfig
import pme123.adapters.server.control.JobActor.JobConfig
import pme123.adapters.server.control.JobParentActor.CreateJobActor
import pme123.adapters.server.entity.ActorMessages.InitActor
import pme123.adapters.shared.{ClientConfig, Logger}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
/**
 * Provide some DI and configuration sugar for new UserActor instances.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
class ClientParentActor @Inject()(@Named("jobParentActor")
                                  jobParentActor: ActorRef
                                  , childFactory: ClientActor.Factory,
                                  configuration: Configuration)
                                 (implicit ec: ExecutionContext)
  extends Actor
    with InjectedActorSupport
    with Logger {

  import ClientParentActor._

  implicit private val timeout: Timeout = Timeout(2.seconds)

  // 1. level of abstraction
  // **************************
  override def receive: Receive = LoggingReceive {
    case RegisterClient(clientConfig: ClientConfig) => registerClient(clientConfig)
    case GetClientConfigs => allClientConfigs()
  }

  // 2. level of abstraction
  // **************************

  private def registerClient(clientConfig: ClientConfig) = {
    val name = s"clientActor-${clientConfig.requestIdent}"
    info(s"Creating ClientActor $name")

    val future =
      (jobParentActor ? CreateJobActor(JobConfig(clientConfig.jobIdent, clientConfig.clientParams)))
        .map(_.asInstanceOf[ActorRef])
        .flatMap { jobActor =>
          val child: ActorRef = injectedChild(childFactory(clientConfig, jobActor), name)
          (child ? InitActor).mapTo[Flow[JsValue, JsValue, _]]
        }

    pipe(future) to sender()
  }

  private def allClientConfigs() {
    val future: Future[Seq[ClientConfig]] = Future.sequence(context.children.toSeq
      .map(ar => (ar ? GetClientConfig)

        .map(_.asInstanceOf[ClientConfig])
      )).map{cc =>
      info(s"allClientConfigs: $cc")
      cc.toList
    }

    pipe(future) to sender()
  }

}

object ClientParentActor {

  case class RegisterClient(clientConfig: ClientConfig)

  case object GetClientConfigs

}

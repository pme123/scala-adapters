package pme123.adapters.server.control

import javax.inject._

import akka.actor._
import akka.stream._
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import pme123.adapters.server.entity.ActorMessages.{InitActor, SubscribeClient, UnSubscribeClient}
import pme123.adapters.shared.ClientConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Creates a initiator actor that sets up the websocket stream.  Although it's not required,
  * having an actor manage the stream helps with lifecycle and monitoring, and also helps
  * with dependency injection through the AkkaGuiceSupport trait.
  *
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  *
  * @param jobActor the actor responsible for the Adapter process
  * @param ec       implicit CPU bound execution context.
  */
class ClientActor @Inject()(@Assisted clientConfig: ClientConfig
                            , @Assisted jobActor: ActorRef)
                           (implicit val mat: Materializer, val ec: ExecutionContext)
  extends UserWebsocket {

  import ClientActor._
  implicit private val timeout: Timeout = Timeout(50.millis)


  // 1. level of abstraction
  // **************************

  override def receive: Receive = {
    case InitActor => init()
    case GetClientConfig =>
      info(s"GetClientConfig: $clientConfig")
      sender() ! clientConfig
    case other =>
      info(s"Unexpected message from ${sender()}: $other")
  }

  /**
    * If this actor is killed directly, stop anything that we started running explicitly.
    * In our case unsubscribe the client in the AdapterActor
    */
  override def postStop(): Unit = {
    info(s"Stopping $clientConfig: actor $self")
    jobActor ! UnSubscribeClient(clientConfig)
  }

  // 2. level of abstraction
  // **************************
  private def init() = {
    info(s"Create Websocket for Client: $clientConfig")
    jobActor ! SubscribeClient(clientConfig, wsActor)
    sender() ! websocketFlow(jobActor)
  }

}

object ClientActor {

  // used to inject the UserActors as childs of the UserParentActor
  trait Factory {
    def apply(clientConfig: ClientConfig, jobActor: ActorRef): Actor
  }

  case object GetClientConfig

}




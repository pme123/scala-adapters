package pme123.adapters.server.entity

import akka.actor.ActorRef
import pme123.adapters.shared.ClientConfig

object ActorMessages {

  case class Create(clientConfig: ClientConfig, processActor: ActorRef)

  case object InitActor

  case class SubscribeClient(clientConfig: ClientConfig, wsActor: ActorRef)

  case class UnSubscribeClient(clientConfig: ClientConfig)

}

package pme123.adapters.server.entity

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActor.ClientIdent
import pme123.adapters.shared.ClientConfig.RequestIdent

object ActorMessages {

  case class Create(id: RequestIdent, processActor: ActorRef)

  case object InitActor

  case class SubscribeClient(clientIdent: ClientIdent, wsActor: ActorRef)

  case class UnSubscribeClient(clientIdent: ClientIdent)

}

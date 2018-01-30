package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.shared.JobConfig.JobIdent

trait JobActorFactory {

  def jobActorFor(jobIdent: JobIdent): ActorRef

}


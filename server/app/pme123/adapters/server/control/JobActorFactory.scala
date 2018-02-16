package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActor.JobDescr
import pme123.adapters.shared.JobConfig.JobIdent

trait JobActorFactory {

  def jobActorFor(jobDescr: JobDescr): ActorRef

  def jobActorFor(jobIdent: JobIdent): ActorRef

  def jobActorsForAll(jobIdent: JobIdent): Seq[ActorRef]
}


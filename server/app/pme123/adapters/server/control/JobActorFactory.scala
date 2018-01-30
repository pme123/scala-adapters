package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.shared.JobConfig.JobIdent

import scala.concurrent.Future

trait JobActorFactory {

  def jobActorFor(jobIdent: JobIdent): ActorRef

}

trait JobFactory {
  def jobProcessFor(jobIdent: JobIdent): JobProcess
}

trait JobProcess {
  protected def runAdapter(user: String): Future[Any]
}

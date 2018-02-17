package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.event.LoggingReceive
import akka.stream.Materializer
import play.api.libs.concurrent.InjectedActorSupport
import pme123.adapters.server.control.JobActor.JobDescr
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared._

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class JobParentActor @Inject()(jobCreation: JobCreation
                               , childFactory: JobActor.Factory
                               , system: ActorSystem
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext)
  extends Actor
    with InjectedActorSupport
    with Logger {

  import JobParentActor._

  private lazy val jobActors: mutable.Map[JobDescr, ActorRef] =
    mutable.Map(jobCreation.createJobActorsOnStartUp().toSeq: _*)

  // 1. level of abstraction
  // **************************
  def receive = LoggingReceive {
    case InitJobParentActor => init()
    case CreateJobActor(jobDescr) => sender() ! getOrCreateJobActor(jobDescr)
    case RemoveJobActor(jobDescr) => //TODO
    case GetAllJobActors(jobIdent) => allJobActorsFor(jobIdent)
    case other => warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************
  // inits the scheduler jobs
  private def init() {
    info("The following Jobs where initiated on startup:")
    jobActors.keys.foreach(jd => s"- ${jd.asString}")
  }

  private def getOrCreateJobActor(jobDescr: JobDescr) =
    jobActors.getOrElse(jobDescr, {
      val jobActor = jobCreation.createJobActor(jobDescr)
      jobActors.put(jobDescr, jobActor)
      jobActor
    })

  private def allJobActorsFor(jobIdent: JobIdent): Seq[ActorRef] =
    jobActors
      .filter { case (k, _) => k.jobIdent == jobIdent }
      .values
      .toSeq

}

object JobParentActor {

  case object InitJobParentActor

  case class CreateJobActor(jobDescr: JobDescr)

  case class RemoveJobActor(jobDescr: JobDescr)

  case class GetAllJobActors(jobIdent: JobIdent)

}

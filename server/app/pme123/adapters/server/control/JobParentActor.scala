package pme123.adapters.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.event.LoggingReceive
import akka.stream.Materializer
import play.api.libs.concurrent.InjectedActorSupport
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

  private lazy val jobActors: mutable.Map[JobConfig, ActorRef] =
    mutable.Map(jobCreation.createJobActorsOnStartUp().toSeq: _*)

  // 1. level of abstraction
  // **************************
  def receive = LoggingReceive {
    case InitJobParentActor => init()
    case CreateJobActor(jobConfig) => sender() ! getOrCreateJobActor(jobConfig)
    case RemoveJobActor(jobConfig) => //TODO
    case GetAllJobConfigs => sender() ! jobActors.keys.toSeq
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

  private def getOrCreateJobActor(jobConfig: JobConfig) =
    jobActors.getOrElse(jobConfig, {
      val jobActor = jobCreation.createJobActor(jobConfig)
      jobActors.put(jobConfig, jobActor)
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

  case class CreateJobActor(jobConfig: JobConfig)

  case class RemoveJobActor(jobConfig: JobConfig)

  case object GetAllJobConfigs

  case class GetAllJobActors(jobIdent: JobIdent)

}

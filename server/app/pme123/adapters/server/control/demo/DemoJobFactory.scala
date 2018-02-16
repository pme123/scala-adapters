package pme123.adapters.server.control.demo

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.JobActor.JobDescr
import pme123.adapters.server.control.JobActorSchedulers.RegisterSchedule
import pme123.adapters.server.control.{JobActor, JobActorFactory}
import pme123.adapters.server.entity.{JobSchedules, ServiceException}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.demo.DemoJobs._

import scala.concurrent.ExecutionContext

@Singleton
class DemoJobFactory @Inject()(demoJob: DemoJobProcess
                               , demoJobWithDefaultScheduler: DemoJobWithDefaultSchedulerActor
                               , demoJobWithoutScheduler: DemoJobWithoutSchedulerActor
                               , @Named("actorSchedulers")
                               actorSchedulers: ActorRef
                               , system: ActorSystem
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext)
  extends JobActorFactory {


  private lazy val demoJobRef = system.actorOf(JobActor.props(demoJobIdent, demoJob))
  private lazy val demoJobWithDefaultSchedulerRef = system.actorOf(JobActor.props(demoJobWithDefaultSchedulerIdent, demoJobWithDefaultScheduler))
  private lazy val demoJobWithoutSchedulerRef = system.actorOf(JobActor.props(demoJobWithoutSchedulerIdent, demoJobWithoutScheduler))

  def jobActorFor(jobDescr: JobDescr): ActorRef = jobActorFor(jobDescr.jobIdent)

  def allJobActorsFor(jobIdent: JobIdent): Seq[ActorRef] = Seq(jobActorFor(jobIdent))

  private def jobActorFor(jobIdent: JobIdent): ActorRef = {
    jobIdent match {
      case "demoJob" => demoJobRef
      case "demoJobWithDefaultScheduler" => demoJobWithDefaultSchedulerRef
      case "demoJobWithoutScheduler" => demoJobWithoutSchedulerRef
      case other => throw ServiceException(s"There is no Job for $other")
    }
  }

  // initiates all JobSchedules
  JobSchedules().schedules.values
    .foreach(schedule =>
      actorSchedulers ! RegisterSchedule(JobDescr(schedule.jobIdent), schedule, jobActorFor(schedule.jobIdent))
    )

}


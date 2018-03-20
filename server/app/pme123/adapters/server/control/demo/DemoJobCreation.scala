package pme123.adapters.server.control.demo

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.{JobActor, JobCreation}
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig
import pme123.adapters.shared.demo.DemoJobs._

import scala.concurrent.ExecutionContext

// the Factory must be a Singleton
@Singleton
class DemoJobCreation @Inject()(demoJob: DemoJobProcess // each JobProcess is injected
                                , demoJobWithDefaultScheduler: DemoJobWithDefaultSchedulerActor
                                , demoJobWithoutScheduler: DemoJobWithoutSchedulerActor
                                , @Named("actorSchedulers") val actorSchedulers: ActorRef // needed to create Schedules
                                , actorSystem: ActorSystem // needed to create the JobActors
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext) // needed for async processing
  extends JobCreation {

  // create all JobActors
  private lazy val demoJobRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobIdent), demoJob), demoJobIdent)
  private lazy val demoJobWithDefaultSchedulerRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobWithDefaultSchedulerIdent), demoJobWithDefaultScheduler), demoJobWithDefaultSchedulerIdent)
  private lazy val demoJobWithoutSchedulerRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobWithoutSchedulerIdent), demoJobWithoutScheduler), demoJobWithoutSchedulerIdent)

  // return the correct JobActor for a JobConfig
  def createJobActor(jobConfig: JobConfig): ActorRef = jobConfig.jobIdent match {
    case "demoJob" => demoJobRef
    case "demoJobWithDefaultScheduler" => demoJobWithDefaultSchedulerRef
    case "demoJobWithoutScheduler" => demoJobWithoutSchedulerRef
    case other => throw ServiceException(s"There is no Job for $other")
  }
}

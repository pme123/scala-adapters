package pme123.adapters.server.control.demo

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.JobActor.JobDescr
import pme123.adapters.server.control.{JobActor, JobCreation}
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.demo.DemoJobs._

import scala.concurrent.ExecutionContext

@Singleton
class DemoJobCreation @Inject()(demoJob: DemoJobProcess
                                , demoJobWithDefaultScheduler: DemoJobWithDefaultSchedulerActor
                                , demoJobWithoutScheduler: DemoJobWithoutSchedulerActor
                                , @Named("actorSchedulers") val actorSchedulers: ActorRef
                                , system: ActorSystem
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext)
  extends JobCreation {

  private lazy val demoJobRef = system.actorOf(JobActor.props(demoJobIdent, demoJob), demoJobIdent)
  private lazy val demoJobWithDefaultSchedulerRef = system.actorOf(JobActor.props(demoJobWithDefaultSchedulerIdent, demoJobWithDefaultScheduler), demoJobWithDefaultSchedulerIdent)
  private lazy val demoJobWithoutSchedulerRef = system.actorOf(JobActor.props(demoJobWithoutSchedulerIdent, demoJobWithoutScheduler), demoJobWithoutSchedulerIdent)

  def createJobActor(jobDescr: JobDescr): ActorRef = jobDescr.jobIdent match {
    case "demoJob" => demoJobRef
    case "demoJobWithDefaultScheduler" => demoJobWithDefaultSchedulerRef
    case "demoJobWithoutScheduler" => demoJobWithoutSchedulerRef
    case other => throw ServiceException(s"There is no Job for $other")
  }
}

package pme123.adapters.server.control.demo

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.{JobActor, JobActorFactory}
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig.JobIdent

import scala.concurrent.ExecutionContext
import pme123.adapters.shared.demo.DemoJobs._

@Singleton
class DemoJobFactory @Inject()(demoJob: DemoJobProcess
                               , demoJobWithDefaultScheduler: DemoJobWithDefaultSchedulerActor
                               , demoJobWithoutScheduler: DemoJobWithoutSchedulerActor
                               , system: ActorSystem
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext)
  extends JobActorFactory {


  private lazy val demoJobRef = system.actorOf(JobActor.props(demoJobIdent, demoJob))
  private lazy val demoJobWithDefaultSchedulerRef = system.actorOf(JobActor.props(demoJobWithDefaultSchedulerIdent, demoJobWithDefaultScheduler))
  private lazy val demoJobWithoutSchedulerRef = system.actorOf(JobActor.props(demoJobWithoutSchedulerIdent, demoJobWithoutScheduler))

  def jobActorFor(jobIdent: JobIdent): ActorRef = jobIdent match {
    case "demoJob" => demoJobRef
    case "demoJobWithDefaultScheduler" => demoJobWithDefaultSchedulerRef
    case "demoJobWithoutScheduler" => demoJobWithoutSchedulerRef
    case other => throw ServiceException(s"There is no Job for $other")
  }

}


package pme123.adapters.server.control.demo

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import pme123.adapters.server.control.actor.JobActorFactory
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig.JobIdent

@Singleton
class DemoJobFactory @Inject()(@Named("demoJob") demoJob: ActorRef
                               , @Named("demoJobWithDefaultScheduler") demoJobWithDefaultScheduler: ActorRef
                               , @Named("demoJobWithoutScheduler") demoJobWithoutScheduler: ActorRef
                              ) extends JobActorFactory {

  def jobActorFor(jobIdent: JobIdent): ActorRef = jobIdent match {
    case "demoJob" => demoJob
    case "demoJobWithDefaultScheduler" => demoJobWithDefaultScheduler
    case "demoJobWithoutScheduler" => demoJobWithoutScheduler
    case other => throw ServiceException(s"There is no Job for $other")
  }

}

object DemoJobFactory {
  val demoJobIdent: JobIdent = "demoJob"
  val demoJobWithDefaultSchedulerIdent: JobIdent = "demoJobWithDefaultScheduler"
  val demoJobWithoutSchedulerIdent: JobIdent = "demoJobWithoutScheduler"
}

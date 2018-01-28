package pme123.adapters.server.control.actor

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig.JobIdent

@Singleton
class TestJobFactory @Inject()(@Named("testJob") testJob: ActorRef
                               , @Named("testJobWithDefaultScheduler") testJobWithDefaultScheduler: ActorRef
                               , @Named("testJobWithoutScheduler") testJobWithoutScheduler: ActorRef) {

  def jobActorFor(jobIdent: JobIdent): ActorRef = jobIdent match {
    case "testJob" => testJob
    case "testJobWithDefaultScheduler" => testJobWithDefaultScheduler
    case "testJobWithoutScheduler" => testJobWithoutScheduler
    case other => throw ServiceException(s"There is no Job for $other")
  }

}

package pme123.adapters.server.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import pme123.adapters.server.control.JobParentActor.InitJobParentActor

@Singleton
class ApplicationInitializer @Inject()(@Named("jobParentActor")
                                       jobParentActor: ActorRef) {

  // makes sure the schedulers for the jobs are initialized
  jobParentActor ! InitJobParentActor

}

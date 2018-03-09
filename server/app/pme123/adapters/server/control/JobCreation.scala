package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActorSchedulers.RegisterSchedule
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs
import pme123.adapters.server.entity.JobSchedule
import pme123.adapters.shared.JobConfig

trait JobCreation {

  def actorSchedulers: ActorRef

  def createJobActor(jobConfig: JobConfig): ActorRef

  def createJobActorsOnStartUp(): Map[JobConfig, ActorRef] = {
    // initiates all JobSchedules from configuration
    jobConfigs
      .map { case (_, jobConfig) =>
        val jobActor = createJobActor(jobConfig)
        initSchedule(jobConfig, jobActor)
        jobConfig -> jobActor
      }
  }

  protected def initSchedule(jobConfig: JobConfig, jobActor: ActorRef) {
    jobConfig.jobSchedule.foreach(sc =>
      actorSchedulers ! RegisterSchedule(jobConfig, JobSchedule(jobConfig.jobIdent, sc), jobActor)
    )
  }
}

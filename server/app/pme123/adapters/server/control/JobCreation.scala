package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActorSchedulers.RegisterSchedule
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs
import pme123.adapters.server.entity.JobSchedules
import pme123.adapters.shared.JobConfig

trait JobCreation {

  def actorSchedulers: ActorRef


  def createJobActor(jobConfig: JobConfig): ActorRef

  def createJobActorsOnStartUp(): Map[JobConfig, ActorRef] = {
    // initiates all JobSchedules
    jobConfigs
      .map { case (jobIdent, _) =>
        val jobConfig = JobConfig(jobIdent)
        val jobActor = createJobActor(jobConfig)
        JobSchedules().schedules
          .get(jobConfig.jobIdent)
          .foreach(schedule => actorSchedulers ! RegisterSchedule(jobConfig, schedule, jobActor))
        jobConfig -> createJobActor(jobConfig)
      }
  }

}

package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActor.JobConfig
import pme123.adapters.server.control.JobActorSchedulers.RegisterSchedule
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigTempls
import pme123.adapters.server.entity.JobSchedules

trait JobCreation {

  def actorSchedulers: ActorRef


  def createJobActor(jobConfig: JobConfig): ActorRef

  def createJobActorsOnStartUp(): Map[JobConfig, ActorRef] = {
    // initiates all JobSchedules
    jobConfigTempls.configs
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

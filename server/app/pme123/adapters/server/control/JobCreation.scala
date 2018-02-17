package pme123.adapters.server.control

import akka.actor.ActorRef
import pme123.adapters.server.control.JobActor.JobDescr
import pme123.adapters.server.control.JobActorSchedulers.RegisterSchedule
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs
import pme123.adapters.server.entity.JobSchedules

trait JobCreation {

  def actorSchedulers: ActorRef


  def createJobActor(jobDescr: JobDescr): ActorRef

  def createJobActorsOnStartUp(): Map[JobDescr, ActorRef] = {
    // initiates all JobSchedules
    jobConfigs.configs
      .map { case (jobIdent, _) =>
        val jobDescr = JobDescr(jobIdent)
        val jobActor = createJobActor(jobDescr)
        JobSchedules().schedules
          .get(jobDescr.jobIdent)
          .foreach(schedule => actorSchedulers ! RegisterSchedule(jobDescr, schedule, jobActor))
        jobDescr -> createJobActor(jobDescr)
      }
  }

}

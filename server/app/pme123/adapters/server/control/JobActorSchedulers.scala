package pme123.adapters.server.control

import java.time.Instant
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import akka.event.LoggingReceive
import akka.stream.Materializer
import pme123.adapters.server.control.JobActor.RunJobFromScheduler
import pme123.adapters.server.entity.JobSchedule
import pme123.adapters.shared._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Part of the adapter framework.
  * - Scheduler that can be configured - see reference.conf:
  * pme123.adapters.job.configs[*].schedule
  *
  */
@Singleton
class JobActorSchedulers @Inject()()
                                  (implicit val mat: Materializer
                                   , val ec: ExecutionContext
                                   , val actorSystem: ActorSystem)
  extends Actor
    with Logger {

  import JobActorSchedulers._


  private val schedulerJobs: mutable.Map[JobConfig, Cancellable] = mutable.Map()
  private val jobActors: mutable.Map[JobConfig, ActorRef] = mutable.Map()

  // 1. level of abstraction
  // **************************
  def receive = LoggingReceive {
    case reg: RegisterSchedule => startJobSchedule(reg)
    case unreg: SchedulerUnregister => stopJobSchedule(unreg.jobConfig)
    case other =>
      warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************


  //noinspection ConvertibleToMethodValue
  // (initNextExecution _) _ is needed - see https://stackoverflow.com/questions/45657747/deprecation-warning-when-compiling-eta-expansion-of-zero-argument-method
  private def startJobSchedule(schReg: RegisterSchedule): Cancellable = {
    stopJobSchedule(schReg.jobConfig) // make sure there is no schedulerjob for this Job description
    info(s"Initialized JobSchedule for ${schReg.jobSchedule}")

    def initNextExecution(jobSchedule: JobSchedule)(): Instant = {

      def init(execDateTime: Instant): Instant = {
        if (execDateTime isBefore Instant.now) {
          init(execDateTime.plusSeconds(jobSchedule.intervalInMin * 60))
        } else {
          execDateTime
        }
      }

      val firstExec = init(jobSchedule.firstTime())

      schReg.jobActor ! SchedulerInfo(
        jobSchedule.jobIdent
        , firstExec
        , jobSchedule.scheduleConfig.firstWeekDay.getOrElse("-")
        , jobSchedule.intervalInMin
      )
      firstExec
    }

    def executionStart: FiniteDuration = {
      val executionInMs = initNextExecution(schReg.jobSchedule).toEpochMilli - Instant.now.toEpochMilli
      executionInMs.millis
    }


    val cancellable = actorSystem.scheduler.schedule(
      executionStart, schReg.jobSchedule.intervalDuration,
      schReg.jobActor, RunJobFromScheduler(initNextExecution(schReg.jobSchedule) _))
    cancellable
  }

  private def stopJobSchedule(jobConfig: JobConfig) {
    schedulerJobs.find(_._1 == jobConfig)
      .map(_._2.cancel())
    schedulerJobs.remove(jobConfig)
    jobActors.remove(jobConfig)
  }

}

object JobActorSchedulers {

  case class RegisterSchedule(jobConfig: JobConfig, jobSchedule: JobSchedule, jobActor: ActorRef)

  case class SchedulerUnregister(jobConfig: JobConfig, jobSchedule: JobSchedule, jobActor: ActorRef)
}


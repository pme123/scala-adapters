package pme123.adapters.server.control

import java.time.Instant
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import akka.event.LoggingReceive
import akka.stream.Materializer
import pme123.adapters.server.control.JobActor.{JobDescr, RunJobFromScheduler}
import pme123.adapters.server.entity.{DateTimeHelper, JobSchedule}
import pme123.adapters.shared._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Part of the adapter framework.
  * - Scheduler that can be configured:
  * pme123.adapters.server.scheduler.execution {
  * // the first time of day the Import should run (this is the Server time!). (format is HH:mm)
  * // Default is 01:00
  *   first.time = "01:00"
  * // the period the Adapter should call the Coop Webservice
  * // Default is one day (1440 minutes) - be aware 1 minute is the smallest period possible
  * // - and it must be greater than the time the import takes!
  * // make also sure that the period is so that the import is at always the same time of day.
  *   period.minutes = 1440
  * }
  */
@Singleton
class JobActorSchedulers @Inject()()
                                  (implicit val mat: Materializer
                                   , val ec: ExecutionContext
                                   , val actorSystem: ActorSystem)
  extends Actor
    with DateTimeHelper
    with Logger {

  import JobActorSchedulers._


  private val schedulerJobs: mutable.Map[JobDescr, Cancellable] = mutable.Map()
  private val jobActors: mutable.Map[JobDescr, ActorRef] = mutable.Map()

  // 1. level of abstraction
  // **************************
  def receive = LoggingReceive {
    case reg: RegisterSchedule => startJobSchedule(reg)
    case unreg: SchedulerUnregister => stopJobSchedule(unreg.jobDescr)
    case other =>
      warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************


  //noinspection ConvertibleToMethodValue
  // (initNextExecution _) _ is needed - see https://stackoverflow.com/questions/45657747/deprecation-warning-when-compiling-eta-expansion-of-zero-argument-method
  private def startJobSchedule(schReg: RegisterSchedule): Cancellable = {
    stopJobSchedule(schReg.jobDescr) // make sure there is no schedulerjob for this Job description
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

  private def stopJobSchedule(jobDescr: JobDescr) {
    schedulerJobs.find(_._1 == jobDescr)
      .map(_._2.cancel())
    schedulerJobs.remove(jobDescr)
    jobActors.remove(jobDescr)
  }

}

object JobActorSchedulers {

  case class RegisterSchedule(jobDescr: JobDescr, jobSchedule: JobSchedule, jobActor: ActorRef)

  case class SchedulerUnregister(jobDescr: JobDescr, jobSchedule: JobSchedule, jobActor: ActorRef)
}


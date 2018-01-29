package pme123.adapters.server.control.actor

import java.time.Instant
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.Materializer
import pme123.adapters.server.control.actor.JobActor.RunAdapterFromScheduler
import pme123.adapters.server.entity.{DateTimeHelper, JobSchedule, JobSchedules}
import pme123.adapters.shared.{Logger, SchedulerInfo}

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
class JobActorScheduler @Inject()(jobFactory: JobActorFactory)
                                 (implicit val mat: Materializer
                                    , val ec: ExecutionContext
                                    , val actorSystem: ActorSystem)
  extends DateTimeHelper
    with Logger {


  //noinspection ConvertibleToMethodValue
  // (initNextExecution _) _ is needed - see https://stackoverflow.com/questions/45657747/deprecation-warning-when-compiling-eta-expansion-of-zero-argument-method
  private def initJobSchedule(jobSchedule: JobSchedule): Cancellable = {
    info(s"Initialized JobSchedule for $jobSchedule")
    val jobActor = jobFactory.jobActorFor(jobSchedule.jobIdent)

    def initNextExecution(jobSchedule: JobSchedule)(): Instant = {

      def init(execDateTime: Instant): Instant = {
        if (execDateTime isBefore Instant.now) {
          init(execDateTime.plusSeconds(jobSchedule.intervalInMin * 60))
        } else {
          execDateTime
        }
      }

      val firstExec = init(jobSchedule.firstTime())

      jobActor ! SchedulerInfo(
        jobSchedule.jobIdent
        , firstExec
        , jobSchedule.scheduleConfig.firstWeekDay.getOrElse("-")
        , jobSchedule.intervalInMin
      )
      firstExec
    }

    def executionStart: FiniteDuration = {
      val executionInMs = initNextExecution(jobSchedule).toEpochMilli - Instant.now.toEpochMilli
      executionInMs.millis
    }


    actorSystem.scheduler.schedule(
      executionStart, jobSchedule.intervalDuration,
      jobActor, RunAdapterFromScheduler(initNextExecution(jobSchedule) _))
  }


  // initiates all JobSchedules
  JobSchedules().schedules.values
    .foreach(initJobSchedule)

}


package pme123.adapters.server.control.actor

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import pme123.adapters.server.control.actor.AdapterActor.RunAdapterFromScheduler
import pme123.adapters.shared.{Logger, SchedulerInfo}
import pme123.adapters.server.entity.AdaptersContext.settings.{schedulerExecutionFirstTime, schedulerExecutionFirstWeekday, schedulerExecutionPeriodInMin}
import pme123.adapters.server.entity.DateTimeHelper

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
object AdapterScheduler {
}

trait AdapterScheduler
  extends DateTimeHelper
    with Logger {
  implicit def actorSystem: ActorSystem
  implicit def ec: ExecutionContext

  def adapterActor: ActorRef

  // possible to have different offsetTimes than configured, e.g. if you have more than one adapter.
  def offsetInMin: Int

  // if needed you can override the default execution time with a number of seconds.
  // This is for smaller imports - that need to be executed a lot.
  def executionPeriodInSec: Long = Int.MaxValue

  // default of the calculated execution period
  def executionPeriodDuration: Option[Long] = Some(executionPeriodInMin)

  // possibility to cancel the scheduler job
  def cancel(): Boolean = importScheduler.cancel()

  private val minExecutionPeriodInMin = 1
  private lazy val executionPeriodInMin = Math.max(minExecutionPeriodInMin, schedulerExecutionPeriodInMin).asInstanceOf[Long]

  private[actor] lazy val executionPeriod = Math.min(executionPeriodInSec, executionPeriodInMin * 60).seconds

  private[actor] lazy val firstExecution = initNextExecution()

  private lazy val executionInMs = firstExecution.toEpochMilli - Instant.now.toEpochMilli
  private lazy val executionStart: FiniteDuration = executionInMs.millis

  //noinspection ConvertibleToMethodValue
  // (initNextExecution _) _ is needed - see https://stackoverflow.com/questions/45657747/deprecation-warning-when-compiling-eta-expansion-of-zero-argument-method
  protected lazy val importScheduler: Cancellable = actorSystem.scheduler.schedule(
    executionStart, executionPeriod,
    adapterActor, RunAdapterFromScheduler(initNextExecution _))

  def initNextExecution(): Instant = {

    def init(execDateTime: Instant): Instant = {
      if (execDateTime isBefore Instant.now) {
        init(execDateTime.plusSeconds(executionPeriod.toSeconds))
      } else {
        execDateTime
      }
    }


    val firstExec = init(schedulerExecutionFirstTime.plus(offsetInMin, ChronoUnit.MINUTES))

    adapterActor ! SchedulerInfo(
      firstExec
      , schedulerExecutionFirstWeekday
      , schedulerExecutionPeriodInMin
    )
    firstExec
  }

}


/**
  * If you don't need a Scheduler, you can use this class that does nothing.
  */
case class NoAdapterScheduler(adapterActor: ActorRef)
                             (implicit val actorSystem: ActorSystem, val ec: ExecutionContext)
  extends AdapterScheduler {

  def offsetInMin = 0

  override lazy val executionPeriodDuration: Option[Long] = None

  override def cancel(): Boolean = true

}

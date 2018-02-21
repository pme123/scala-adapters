package pme123.adapters.server.control.demo

import javax.inject.Inject

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import play.api.libs.json.{JsValue, Json}
import pme123.adapters.server.control.JobActor.{ClientsChange, LastResult}
import pme123.adapters.server.control.{JobProcess, LogService}
import pme123.adapters.server.entity.AConcreteResult
import pme123.adapters.shared.ClientConfig.Filterable
import pme123.adapters.shared.LogLevel.{DEBUG, ERROR, INFO, WARN}
import pme123.adapters.shared._
import pme123.adapters.shared.demo.DemoResult

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait DemoProcess extends JobProcess {

  def jobLabel: String

  def createInfo(): ProjectInfo =
    createInfo(pme123.adapters.version.BuildInfo.version, Nil)

  // the process fakes some long taking tasks that logs its progress
  def runJob(user: String)
            (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.startLogging()
     DemoService.results
          .foreach(doSomeWork)

      val results = if (Random.nextBoolean()) DemoService.results else DemoService.results.reverse
      (jobActor ? LastResult(DemoResults(results)))
        .map{
          case ClientsChange(clientConfigs) =>
            logService.info(s"The results of ${clientConfigs.size} Clients have changed: ${clientConfigs.map(_.requestIdent).mkString("(", ", ", ")")}.")
        }
      logService
    }
  }

  protected def doSomeWork(dr: DemoResult)
                          (implicit logService: LogService): LogEntry = {
    Thread.sleep(750)
    val ll = Random.shuffle(List(DEBUG, DEBUG, INFO, INFO, INFO, WARN, WARN, ERROR)).head
    val detail = List(None, Some(s"Details for $jobLabel $ll: ${dr.name}"), Some("shell_session_save_history () \n{ \n    shell_session_history_enable;\n    history -a;\n    if [ -f \"$SHELL_SESSION_HISTFILE_SHARED\" ] && [ ! -s \"$SHELL_SESSION_HISTFILE\" ]; then\n        echo -ne '\\n...copying shared history...';\n        ( umask 077;\n        /bin/cp \"$SHELL_SESSION_HISTFILE_SHARED\" \"$SHELL_SESSION_HISTFILE\" );\n    fi;\n    echo -ne '\\n...saving history...';\n    ( umask 077;\n    /bin/cat \"$SHELL_SESSION_HISTFILE_NEW\" >> \"$SHELL_SESSION_HISTFILE_SHARED\" );\n    ( umask 077;\n    /bin/cat \"$SHELL_SESSION_HISTFILE_NEW\" >> \"$SHELL_SESSION_HISTFILE\" );\n    : >|\"$SHELL_SESSION_HISTFILE_NEW\";\n    if [ -n \"$HISTFILESIZE\" ]; then\n        echo -n 'truncating history files...';\n        HISTFILE=\"$SHELL_SESSION_HISTFILE_SHARED\";\n        HISTFILESIZE=\"$HISTFILESIZE\";\n        HISTFILE=\"$SHELL_SESSION_HISTFILE\";\n        HISTFILESIZE=\"$size\";\n        HISTFILE=\"$SHELL_SESSION_HISTFILE_NEW\";\n    fi;\n    echo -ne '\\n...'\n}"))(Random.nextInt(3))
    logService.log(ll, s"Job: $jobLabel $ll: ${dr.name}", detail)
  }
}

class DemoJobProcess @Inject()()(implicit val mat: Materializer, val ec: ExecutionContext)
  extends DemoProcess {
  override def jobLabel: String = "Demo Job"
}

class DemoJobWithDefaultSchedulerActor @Inject()()(implicit val mat: Materializer, val ec: ExecutionContext)
  extends DemoProcess {
  val jobLabel = "Demo Job with Default Scheduler"
}

class DemoJobWithoutSchedulerActor @Inject()()(implicit val mat: Materializer, val ec: ExecutionContext)
  extends DemoProcess {
  val jobLabel = "Demo Job without Scheduler"

  // send result on each step
  override def runJob(user: String)
                     (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.startLogging()
      jobActor ! GenericResults(Nil) // reset last result

      DemoService.results.foreach { dr =>
        doSomeWork(dr)
        jobActor ! GenericResult(Json.toJson(dr))
      }
      logService
    }
  }

}

case class DemoResults(results: Seq[DemoResult]) extends AConcreteResult {

  // type class instance for ImageElem
  implicit object filterableDemoResult extends Filterable[DemoResult] {
     def sortBy(filterable: DemoResult): String = filterable.name

     def doFilter(filterable: DemoResult)(implicit filters: Map[String, String]): Boolean = {
       matchText("name", filterable.name) &&
         matchText("imgUrl", filterable.imgUrl) &&
         matchDate(dateTimeAfterL, filterable.created, (a, b) => a <= b) &&
         matchDate(dateTimeBeforeL, filterable.created, (a, b) => a >= b)
     }

  }

  def clientFiltered(clientConfig: ClientConfig): Seq[JsValue] =
    ClientConfig.filterResults(results, clientConfig)
      .map(dr => Json.toJson(dr))
}
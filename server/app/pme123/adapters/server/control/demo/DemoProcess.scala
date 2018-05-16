package pme123.adapters.server.control.demo

import java.time.LocalDateTime

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import pme123.adapters.server.control.JobActor.{ClientsChange, LastResult}
import pme123.adapters.server.control.demo.DemoService.toISODateTimeString
import pme123.adapters.server.control.{JobProcess, LogService}
import pme123.adapters.server.entity.JsonParseException
import pme123.adapters.server.entity.demo.{DemoAdapterContext, DemoAdapterSettings, DemoResults}
import pme123.adapters.shared.LogLevel.{DEBUG, ERROR, INFO, WARN}
import pme123.adapters.shared._
import pme123.adapters.shared.demo.{DemoResult, ImageUpload}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait DemoProcess
  extends JobProcess
    with Logger {

  def jobLabel: String

  def createInfo(): ProjectInfo = // same version as the adapters!
    createInfo(pme123.adapters.version.BuildInfo.version
      , AdaptersContextProps(DemoAdapterSettings.configPath, DemoAdapterContext.props)
    )

  def runJob(user: String)
            (implicit logService: LogService, jobActor: ActorRef): Future[LogService] =
    throw new UnsupportedOperationException("this is not supported by the Demo Job Process")

  // the process fakes some long taking tasks that logs its progress
  override def runJob(user: String, payload: Option[JsValue])
                     (implicit logService: LogService, jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.startLogging()
      DemoService.results
          .foreach(doSomeWork)
      val handledImage = handleImage(payload)

      val results = if (Random.nextBoolean()) DemoService.results else DemoService.results.reverse
      (jobActor ? LastResult(DemoResults(handledImage ++ results)))
        .map{
          case ClientsChange(clientConfigs) =>
            logService.info(s"The results of ${clientConfigs.size} Clients have changed: ${clientConfigs.map(_.requestIdent).mkString("(", ", ", ")")}.")
        }
      logService
    }
  }

  protected def handleImage(payload: Option[JsValue])(implicit logService: LogService): List[DemoResult] = {
    payload.map(p => Json.fromJson[ImageUpload](p) match {
      case JsSuccess(iu, _) => iu
      case JsError(errors) =>
        val errMsg = s"Problem parsing UploadImage: ${errors.map(e => s"${e._1} -> ${e._2}")}"
        logService.error(errMsg)
        throw JsonParseException(errMsg)
    }).map { iu =>
      logService.info(s"Image imported: ${iu.descr}")
      DemoResult(iu.descr, Right(iu.imgData), toISODateTimeString(LocalDateTime.now()))
    }.toList
  }

  protected def doSomeWork(dr: DemoResult)
                          (implicit logService: LogService): LogEntry = {
    Thread.sleep(750)
    val ll = Random.shuffle(List(DEBUG, DEBUG, INFO, INFO, INFO, WARN, WARN, ERROR)).head
    val detail = List(None, Some(s"Details for $jobLabel $ll: ${dr.name}"), Some("shell_session_save_history () \n{ \n    shell_session_history_enable;\n    history -a;\n    if [ -f \"$SHELL_SESSION_HISTFILE_SHARED\" ] && [ ! -s \"$SHELL_SESSION_HISTFILE\" ]; then\n        echo -ne '\\n...copying shared history...';\n        ( umask 077;\n        /bin/cp \"$SHELL_SESSION_HISTFILE_SHARED\" \"$SHELL_SESSION_HISTFILE\" );\n    fi;\n    echo -ne '\\n...saving history...';\n    ( umask 077;\n    /bin/cat \"$SHELL_SESSION_HISTFILE_NEW\" >> \"$SHELL_SESSION_HISTFILE_SHARED\" );\n    ( umask 077;\n    /bin/cat \"$SHELL_SESSION_HISTFILE_NEW\" >> \"$SHELL_SESSION_HISTFILE\" );\n    : >|\"$SHELL_SESSION_HISTFILE_NEW\";\n    if [ -n \"$HISTFILESIZE\" ]; then\n        echo -n 'truncating history files...';\n        HISTFILE=\"$SHELL_SESSION_HISTFILE_SHARED\";\n        HISTFILESIZE=\"$HISTFILESIZE\";\n        HISTFILE=\"$SHELL_SESSION_HISTFILE\";\n        HISTFILESIZE=\"$size\";\n        HISTFILE=\"$SHELL_SESSION_HISTFILE_NEW\";\n    fi;\n    echo -ne '\\n...'\n}"))(Random.nextInt(3))
    (for(_ <- 0 to 20) yield logService.log(ll, s"Job: $jobLabel $ll: ${dr.name}", detail))
      .head
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
  override def runJob(user: String, payload: Option[JsValue])
                     (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.startLogging()
      jobActor ? LastResult(DemoResults(Nil)) // reset last result
      jobActor ? handleImage(payload).map(dr => LastResult(DemoResults(Seq(dr)), append = true))

      DemoService.results.foreach { dr =>
        doSomeWork(dr)
        jobActor ? LastResult(DemoResults(Seq(dr)), append = true)
      }
      logService
    }
  }

}

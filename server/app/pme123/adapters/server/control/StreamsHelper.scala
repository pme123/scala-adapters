package pme123.adapters.server.control

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.stream.{ActorAttributes, Attributes, Supervision}
import akka.util.ByteString
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.mvc.Http.Status
import pme123.adapters.server.control.http.{WebAccessForbiddenException, WebBadStatusException, WebNotAcceptableException, WebNotFoundException}
import pme123.adapters.server.entity.{AdaptersException, JsonParseException}
import pme123.adapters.shared.LogLevel.DEBUG
import pme123.adapters.shared.{LogLevel, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
  * Created by pascal.mengelt on 11.10.2016.
  */
trait StreamsHelper
  extends JsonHelper
    with Logger {
  val nrOfWorker = 10
  val maximumFrameLength = 10000

  implicit def ec: ExecutionContext

  protected lazy val unmarshalToString: Flow[ByteString, String, NotUsed] = {
    Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength, allowTruncation = true))
      .map(_.utf8String)
      .fold("")((a, b) => s"$a\n$b")
  }

  protected lazy val extractResponse: Flow[WSResponse, WSResponse, NotUsed] =
    Flow[WSResponse].map { resp =>
      checkStatus(resp)
    }

  protected def checkStatus(resp: WSResponse): WSResponse = {
    resp.status match {
      case Status.OK => resp
      case Status.NOT_FOUND => throw WebNotFoundException()
      case Status.FORBIDDEN | Status.UNAUTHORIZED => throw WebAccessForbiddenException()
      case Status.NOT_ACCEPTABLE => throw WebNotAcceptableException()
      case _ => throw WebBadStatusException(resp.status + " - " + resp.statusText.toString)
    }
  }

  protected def logResponse(label: String, logLevel: LogLevel = DEBUG)
                           (implicit logService: LogService): Flow[WSResponse, WSResponse, NotUsed] =
    Flow[WSResponse]
      .map { resp =>
        Future(
          logService.log(logLevel, label, Some(Json.prettyPrint(resp.json)))
        )
        resp
      }

  /**
    * changes a Try to a Future to better include in a Flow.
    * And changing from blocking to non-blocking.
    */
  protected def tryToFuture[A](t: => Try[A]): Future[A] = {
    Future {
      t
    }.flatMap {
      case Success(s) => Future.successful(s)
      case Failure(fail) => Future.failed(fail)
    }
  }

  protected def supervisionStrategy(label: String)(implicit logService: LogService): Attributes = {
    ActorAttributes.supervisionStrategy(supervisionDecider(label))
  }

  private def supervisionDecider(label: String)(implicit logService: LogService): Supervision.Decider = {
    case exc: AdaptersException =>
      logService.warn(s"$label - stream resumed: ${exc.msg}", Some(exc.allErrorMsgs))
      error(exc, "Exception from the warning above.")
      Supervision.Resume
    case NonFatal(exc) =>
      logService.error(exc, s"$label - stream resumed: $exc")
      Supervision.Resume
    case exc: Exception =>
      logService.error(exc, s"$label - stream resumed: $exc")
      Supervision.Stop
  }

}

trait JsonHelper {

  /**
    * helper that handles a JSON result and its possible validation failures.
    *
    * @param jsResult the json result to handle.
    * @tparam A type of the expected result.
    * @return the result if successful.
    */
  protected def handleJson[A](jsResult: JsResult[A]): A = {
    jsResult match {
      case JsSuccess(result, _) => result
      case e: JsError => throw JsonParseException(s"Json Parse Errors: ${JsError.toJson(e)}")
    }
  }
}

package pme123.adapters.server.boundary

import javax.inject.{Inject, Named}

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import controllers.Assets._
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.server.control.http.SameOriginCheck
import pme123.adapters.server.entity.ActorMessages.Create
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WebsocketController @Inject()(jobFactory: JobActorFactory
                                    , @Named("userParentActor")
                                    userParentActor: ActorRef
                                    , assetsFinder: AssetsFinder
                                    , cc: ControllerComponents
                                    , val config: Configuration)
                                   (implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with SameOriginCheck
    with Logger {

  implicit val timeout: Timeout = Timeout(1.second) // the first run in dev can take a while :-(

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws(jobIdent: JobIdent): WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      val actor = jobFactory.jobActorFor(jobIdent)
      wsFutureFlow(rh, actor).map { flow =>
        Right(flow)
      }.recover {
        case e: Exception =>
          error(e, "Cannot create websocket")
          val jsError = Json.obj("error" -> "Cannot create websocket")
          val result = InternalServerError(jsError)
          Left(result)
      }

    case rejected =>
      error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * Creates a Future containing a Flow of JsValue in and out.
    */
  private def wsFutureFlow(request: RequestHeader, adapter: ActorRef): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    val future: Future[Any] = userParentActor ? Create(request.id.toString, adapter)
    val futureFlow: Future[Flow[JsValue, JsValue, NotUsed]] = future.mapTo[Flow[JsValue, JsValue, NotUsed]]
    futureFlow
  }

}
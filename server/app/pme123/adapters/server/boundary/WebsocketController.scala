package pme123.adapters.server.boundary

import javax.inject._

import akka.NotUsed
import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl._
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.actor.UserParentActor
import pme123.adapters.server.control.http.SameOriginCheck

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class WebsocketController @Inject()(@Named("userParentActor") userParentActor: ActorRef
                                    , cc: ControllerComponents
                                    , val config: Configuration)
                                   (implicit ec: ExecutionContext)
  extends AbstractController(cc) with SameOriginCheck {

  val logger = play.api.Logger(getClass)

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder APIs
    Ok("WebsocketController Test Page")
  }

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws(adapter: ActorRef): WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      wsFutureFlow(rh, adapter).map { flow =>
        Right(flow)
      }.recover {
        case e: Exception =>
          logger.error("Cannot create websocket", e)
          val jsError = Json.obj("error" -> "Cannot create websocket")
          val result = InternalServerError(jsError)
          Left(result)
      }

    case rejected =>
      logger.error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * Creates a Future containing a Flow of JsValue in and out.
    */
  private def wsFutureFlow(request: RequestHeader, adapter: ActorRef): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    implicit val timeout: Timeout = Timeout(1.second) // the first run in dev can take a while :-(
    val future: Future[Any] = userParentActor ? UserParentActor.Create(request.id.toString, adapter)
    val futureFlow: Future[Flow[JsValue, JsValue, NotUsed]] = future.mapTo[Flow[JsValue, JsValue, NotUsed]]
    futureFlow
  }

}


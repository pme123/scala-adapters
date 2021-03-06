package pme123.adapters.server.boundary

import javax.inject.{Inject, Named}

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import pme123.adapters.server.control.ClientParentActor.RegisterClient
import pme123.adapters.server.control.http.SameOriginCheck
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.{ClientConfig, JobConfig, Logger}
import pme123.adapters.server.entity.AdaptersContext.settings
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WebsocketController @Inject()(@Named("clientParentActor")
                                    clientParentActor: ActorRef
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
  def ws(jobIdent: JobIdent
         , subWebpath: Option[String]
         , resultCount: Option[Int]
         , resultFilter: Option[String]): WebSocket =
    websocket(
      settings.jobConfigs(jobIdent).copy(subWebPath =
        subWebpath.getOrElse(""))
      , resultCount.getOrElse(ClientConfig.defaultResultCount)
      , resultFilter
    )

  def websocket(jobConfig: JobConfig
                , resultCount: Int = ClientConfig.defaultResultCount
                , resultFilter: Option[String] = None
               ): WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      val config = ClientConfig(rh.id.toString, jobConfig, resultCount, resultFilter)
      wsFutureFlow(config).map { flow =>
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
  private def   wsFutureFlow(clientConfig: ClientConfig): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    val future: Future[Any] = clientParentActor ? RegisterClient(clientConfig)
    val futureFlow: Future[Flow[JsValue, JsValue, NotUsed]] = future.mapTo[Flow[JsValue, JsValue, NotUsed]]
    futureFlow
  }

}
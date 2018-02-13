package pme123.adapters.server.boundary

import javax.inject._

import akka.actor._
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.server.entity.AdaptersContext.settings

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class GenericResultController @Inject()(val jobFactory: JobActorFactory
                                        , @Named("userParentActor")
                                     val userParentActor: ActorRef
                                        , template: views.html.adapters.demo
                                        , assetsFinder: AssetsFinder
                                        , cc: ControllerComponents
                                        , val config: Configuration)
                                       (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with WebsocketController {

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, RESULT_CLIENT, assetsFinder))
  }

}


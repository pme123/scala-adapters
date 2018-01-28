package pme123.adapters.server.boundary

import javax.inject._

import akka.actor._
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import pme123.adapters.server.control.actor.TestJobFactory
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class TestController @Inject()(jobFactory: TestJobFactory
                               , val cc: ControllerComponents
                               , template: views.html.adapters.index
                               , assetsFinder: AssetsFinder
                               , val config: Configuration
                               , websocketController: WebsocketController
                               )
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  val logger = play.api.Logger(getClass)

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(assetsFinder))
  }

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @param adapterJob is to differentiate different adapters.
    *                Here we only have one - so it is not needed.
    * @return a fully realized websocket.
    */
  def ws(adapterJob: String): WebSocket = {
    val actor = jobFactory.jobActorFor(adapterJob)
    websocketController.ws(actor)
  }


}

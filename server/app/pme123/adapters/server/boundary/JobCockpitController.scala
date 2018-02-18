package pme123.adapters.server.boundary

import javax.inject._

import akka.actor.ActorRef
import akka.pattern.ask
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.ClientParentActor.GetClientConfigs
import pme123.adapters.server.entity.AdaptersContext.settings
import pme123.adapters.server.entity.JOB_CLIENT
import pme123.adapters.shared.ClientConfig

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class JobCockpitController @Inject()(@Named("clientParentActor")
                                     val clientParentActor: ActorRef
                                     , template: views.html.adapters.demo
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_CLIENT, assetsFinder))
  }

  def jobConfigTempls(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    info(s"called jobConfigTempls: ${settings.jobConfigTempls}")
    Ok(Json.toJson(settings.jobConfigTempls)).as(JSON)
  }

  def clientConfigs(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (clientParentActor ? GetClientConfigs)
      .map(_.asInstanceOf[Seq[ClientConfig]])
      .map(clients => Ok(Json.toJson[Seq[ClientConfig]](clients)))
  }

}


package pme123.adapters.server.boundary

import javax.inject._

import akka.actor._
import akka.pattern.ask
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.JobActor.{ClientConfigs, GetClientConfigs}
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.server.entity.AdaptersContext.settings
import pme123.adapters.shared.ClientConfig
import pme123.adapters.shared.JobConfig.JobIdent

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class JobCockpitController @Inject()(val jobFactory: JobActorFactory
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
    Ok(template(context, JOB_CLIENT, assetsFinder))
  }



  def jobConfigs(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    info(s"called jobConfigs: ${settings.jobConfigs}")
    Ok(Json.toJson(settings.jobConfigs)).as(JSON)
  }

  def clientConfigs(jobIdent: JobIdent): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    info(s"called clientConfigs for job $jobIdent")
    (jobFactory.jobActorFor(jobIdent) ? GetClientConfigs)
      .map(_.asInstanceOf[ClientConfigs])
      .map(_.clientConfigs)
      .map(clients => Ok(Json.toJson[Seq[ClientConfig]](clients)))
  }



}


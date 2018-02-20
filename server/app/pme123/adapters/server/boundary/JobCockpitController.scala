package pme123.adapters.server.boundary

import javax.inject._

import akka.actor.ActorRef
import akka.pattern.ask
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.ClientParentActor.GetClientConfigs
import pme123.adapters.server.control.JobParentActor.GetAllJobConfigs
import pme123.adapters.server.entity.AdaptersContext.settings
import pme123.adapters.server.entity.{JOB_CLIENT, ObjectExpectedException}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.{ClientConfig, JobConfig}

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class JobCockpitController @Inject()(@Named("clientParentActor")
                                     val clientParentActor: ActorRef
                                     , @Named("jobParentActor")
                                     jobParentActor: ActorRef
                                     , template: views.html.adapters.demo
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  def jobProcess(jobIdent: JobIdent) = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_CLIENT
      , s"/$jobIdent"
      , assetsFinder))
  }

  def jobConfigs(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (jobParentActor ? GetAllJobConfigs)
      .map {
        case jobConfigs: Seq[JobConfig] =>
          Ok(Json.toJson(jobConfigs)).as(JSON)
        case other => throw ObjectExpectedException(s"Get all JobConfigs returned an unexpected result: $other")
      }
  }

  def clientConfigs(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (clientParentActor ? GetClientConfigs)
      .map(_.asInstanceOf[Seq[ClientConfig]])
      .map(clients => Ok(Json.toJson[Seq[ClientConfig]](clients)))
  }

}


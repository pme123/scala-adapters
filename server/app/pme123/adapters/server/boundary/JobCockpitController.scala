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
import pme123.adapters.shared.{JOB_PROCESS, JOB_RESULTS}
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
                                     , template: views.html.adapters.index
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  def jobProcess(jobIdent: JobIdent) = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_PROCESS
      , s"/$jobIdent"
      , assetsFinder))
  }

  def jobResults(jobIdent: JobIdent) = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_RESULTS
      , s"/$jobIdent"
      , assetsFinder))
  }

  def jobConfigs(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (jobParentActor ? GetAllJobConfigs)
      .map(_.asInstanceOf[Seq[JobConfig]])
      .map(jobConfigs =>
          Ok(Json.toJson(jobConfigs)).as(JSON)
      )
  }

  def clientConfigs(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (clientParentActor ? GetClientConfigs)
      .map(_.asInstanceOf[Seq[ClientConfig]])
      .map(clients => Ok(Json.toJson[Seq[ClientConfig]](clients)))
  }

}


package pme123.adapters.server.boundary

import javax.inject._

import akka.actor.ActorRef
import akka.pattern.ask
import controllers.AssetsFinder
import play.Environment
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import pme123.adapters.server.control.ClientParentActor.GetClientConfigs
import pme123.adapters.server.control.JobParentActor.GetAllJobConfigs
import pme123.adapters.server.entity.AdaptersContext.settings
import pme123.adapters.server.entity.ProjectConfig
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared._

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
                                     , env: Environment
                                     , val cc: ControllerComponents
                                     , val config: Configuration
                                     , val accessControl: AccessControl)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController
    with Secured {

  private lazy val firstJobConfig = settings.jobConfigs.keys.headOption.getOrElse("defaultJob")

  def index(): Action[AnyContent] = defaultJobProcess()

  def defaultJobProcess(): Action[AnyContent] = jobProcess(firstJobConfig)

  def jobProcess(jobIdent: JobIdent) = AuthenticatedAction { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(ProjectConfig(context, JOB_PROCESS, s"/$jobIdent", env.isDev)
      , assetsFinder))
  }

  def defaultJobResults(): Action[AnyContent] = jobResults(firstJobConfig)

  def jobResults(jobIdent: JobIdent) = AuthenticatedAction { implicit request: Request[AnyContent] =>
    Ok(template(ProjectConfig(context, JOB_RESULTS, s"/$jobIdent", env.isDev)
      , assetsFinder))
  }

  def defaultCustomPage(): Action[AnyContent] = customPage(firstJobConfig)

  def customPage(jobIdent: JobIdent) = AuthenticatedAction { implicit request: Request[AnyContent] =>
    Ok(template(ProjectConfig(context, CUSTOM_PAGE, s"/$jobIdent", env.isDev)
      , assetsFinder))
  }

  def jobConfigs(): Action[AnyContent] = AuthenticatedAction.async { implicit request: Request[AnyContent] =>
    (jobParentActor ? GetAllJobConfigs)
      .map(_.asInstanceOf[Seq[JobConfig]])
      .map(jobConfigs =>
          Ok(Json.toJson(jobConfigs)).as(JSON)
      )
  }

  def clientConfigs(): Action[AnyContent] = AuthenticatedAction.async { implicit request: Request[AnyContent] =>
    (clientParentActor ? GetClientConfigs)
      .map(_.asInstanceOf[Seq[ClientConfig]])
      .map(clients => Ok(Json.toJson[Seq[ClientConfig]](clients)))
  }

}


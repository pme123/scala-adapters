package pme123.adapters.server.boundary.demo

import javax.inject._

import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.{AdaptersController, JobCockpitController}
import pme123.adapters.server.entity.ProjectConfig
import pme123.adapters.shared.CUSTOM_PAGE
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.demo.DemoJobs.demoJobIdent

import scala.concurrent.ExecutionContext
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class DemoResultController @Inject()(jobController: JobCockpitController
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {


  def index: Action[AnyContent] = jobController.jobProcess(demoJobIdent)

  // Home page that renders template
  def defaultResults: Action[AnyContent] = jobController.customPage(demoJobIdent)

}


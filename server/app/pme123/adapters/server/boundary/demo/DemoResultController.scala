package pme123.adapters.server.boundary.demo

import javax.inject._

import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.{AdaptersController, JobCockpitController}
import pme123.adapters.shared.demo.DemoJobs.demoJobIdent

import scala.concurrent.ExecutionContext

@Singleton
class DemoResultController @Inject()(jobController: JobCockpitController
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  // Home page that renders template / not needed - is only here for demo purposes
  def defaultResults: Action[AnyContent] = jobController.customPage(demoJobIdent)

}


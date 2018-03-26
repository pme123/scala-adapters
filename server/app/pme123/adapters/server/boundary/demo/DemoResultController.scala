package pme123.adapters.server.boundary.demo

import javax.inject._
import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.{AdaptersController, JobCockpitController, WebsocketController}
import pme123.adapters.shared.ClientConfig
import pme123.adapters.shared.demo.DemoJobs.demoJobIdent
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs

import scala.concurrent.ExecutionContext

@Singleton
class DemoResultController @Inject()(jobController: JobCockpitController
                                     , wsController: WebsocketController
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  // Home page that renders template / not needed - is only here for demo purposes
  def defaultResults: Action[AnyContent] = jobController.customPage(demoJobIdent)

  def ws(dynamicIdent: String
         , resultCount: Option[Int]
         , resultFilter: Option[String]): WebSocket = {
    wsController.websocket(
      jobConfigs(demoJobIdent).copy(subWebPath = s"/$dynamicIdent")
      , resultCount = resultCount.getOrElse(ClientConfig.defaultResultCount)
      , resultFilter = resultFilter
    )
  }
}


package pme123.adapters.server.boundary.demo

import javax.inject._

import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.AdaptersController
import pme123.adapters.server.entity.{JOB_CLIENT, RESULT_CLIENT}
import pme123.adapters.shared.JobConfig.JobIdent

import scala.concurrent.ExecutionContext
import pme123.adapters.shared.demo.DemoJobs.demoJobIdent
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class DemoResultController @Inject()(template: views.html.adapters.demo
                                     , assetsFinder: AssetsFinder
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {


  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_CLIENT
      , s"/$demoJobIdent"
      , assetsFinder))
  }
  // Home page that renders template
  def demoResults = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, RESULT_CLIENT
      , s"/$demoJobIdent"
      , assetsFinder))
  }

}


package pme123.adapters.server.boundary.demo

import javax.inject._

import akka.actor._
import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.AdaptersController
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.server.entity.RESULT_CLIENT

import scala.concurrent.ExecutionContext

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class DemoResultController @Inject()(val jobFactory: JobActorFactory
                                        , @Named("userParentActor")
                                     val userParentActor: ActorRef
                                        , template: views.html.adapters.demo
                                        , assetsFinder: AssetsFinder
                                        , cc: ControllerComponents
                                        , val config: Configuration)
                                       (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  // Home page that renders template
  def demoResults = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, RESULT_CLIENT, assetsFinder))
  }

}


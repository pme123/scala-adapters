package pme123.adapters.server.boundary.demo

import javax.inject._

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import pme123.adapters.server.boundary.JobCockpitController
import pme123.adapters.server.control.actor.{JobActorFactory, UserParentActor}
import pme123.adapters.server.control.http.SameOriginCheck
import pme123.adapters.shared.JobConfig.JobIdent

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
@Singleton
class DemoController @Inject()(val cc: ControllerComponents
                               , template: views.html.adapters.index
                               , assetsFinder: AssetsFinder
                               , val config: Configuration
                               , websocketController: JobCockpitController
                               )
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with SameOriginCheck {

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(assetsFinder))
  }



}

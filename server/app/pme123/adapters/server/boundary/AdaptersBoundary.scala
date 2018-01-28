package pme123.adapters.server.boundary

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc._
import pme123.adapters.server.entity.AdaptersContext.settings
import pme123.adapters.shared.Logger

import scala.concurrent.ExecutionContext

/**
  * Boundary for general API for the clients
  */
@Singleton
class AdaptersBoundary @Inject()(val cc: ControllerComponents)
                                (implicit ec: ExecutionContext)
  extends AbstractController(cc)
with Logger {

  def jobConfigs(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    info(s"called jobConfigs: ${settings.jobConfigs}")
     Ok(Json.toJson(settings.jobConfigs)).as(JSON)
  }


}

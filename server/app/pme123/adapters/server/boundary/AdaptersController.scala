package pme123.adapters.server.boundary

import akka.util.Timeout
import pme123.adapters.server.entity.AdaptersContext.settings.httpContext
import pme123.adapters.shared.Logger

import scala.concurrent.duration._

trait AdaptersController
  extends Logger {

  implicit val timeout: Timeout = Timeout(1.second) // the first run in dev can take a while :-(

  protected def context: String = {
    val context = if (httpContext.length > 1)
      httpContext
    else
      ""
    context
  }
}

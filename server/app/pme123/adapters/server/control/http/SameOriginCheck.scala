package pme123.adapters.server.control.http

import play.api.Configuration
import play.api.mvc.RequestHeader
import pme123.adapters.server.entity.AdaptersContext
import pme123.adapters.shared.Logger

trait SameOriginCheck
  extends Logger{

  def config: Configuration

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        error(s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin")
        false

      case None =>
        error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    *
    * see reference.conf: wsocket.hosts.allowed for a description.
    */
  def originMatches(origin: String): Boolean = {

    val allowedHosts = AdaptersContext.settings.wsocketHostsAllowed
    allowedHosts.exists(origin.startsWith)
  }

}

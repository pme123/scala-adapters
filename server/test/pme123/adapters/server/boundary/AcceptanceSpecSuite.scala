package pme123.adapters.server.boundary

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice._

/*
 * This collects all Specs that need the Server setup to optimize testing (only one server initializing).
 * Created by pascal.mengelt on 01.11.2016.
 */
class AcceptanceSpecSuite extends PlaySpec with GuiceOneAppPerSuite {

override def nestedSuites = Vector(
  // no tests yet
)

// Override app if you need an Application with other than non-default parameters.
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map("ehcacheplugin" -> "disabled")).build()

}

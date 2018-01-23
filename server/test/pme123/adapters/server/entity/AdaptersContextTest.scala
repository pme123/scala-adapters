package pme123.adapters.server.entity

import pme123.adapters.server.entity.AdaptersContext.settings._

class AdaptersContextTest
    extends UnitTest {

  val protocol = "http"
  val mailLoglevel = "info"

  "The ServerConfig" should "have values for defined properties" in {
    assert(adminMailRecipient.trim.nonEmpty)
    assert(adminMailLoglevel.level.trim.nonEmpty)
  }

  val port = 22
  "The Server URLs" should "be correctly created" in {
    val sslEnabled = false
    createUrl("host", port, sslEnabled) should be("http://host:22")
  }
  it should "have interpreted ssl.enabled correctly" in {
    val sslEnabled = true
    createUrl("host", port, sslEnabled) should be("https://host:22")
  }
}

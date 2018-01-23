package pme123.adapters.server.control

class AdaptersConfigSpec
extends GuiceAcceptanceSpec {

  private val aConfig = inject[AdaptersConfig]

  val protocol = "http"
  val mailLoglevel = "info"

  "The ServerConfig should have values for defined properties" in {
    assert(aConfig.adminMailRecipient.trim.nonEmpty)
    assert(aConfig.adminMailLoglevel.level.trim.nonEmpty)
  }

  val httpPort = 22
  "The Server URLs should be correctly created" in {
    val sslEnabled = false
    aConfig.createUrl("host", httpPort, sslEnabled) must be("http://host:22")
  }
  "It should have interpreted ssl.enabled correctly" in {
    val sslEnabled = true
    aConfig.createUrl("host", httpPort, sslEnabled) must be("https://host:22")
  }
}

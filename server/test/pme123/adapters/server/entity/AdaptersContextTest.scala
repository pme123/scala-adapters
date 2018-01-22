package pme123.adapters.server.entity

import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.AdaptersSettings._

class AdaptersContextTest
    extends UnitTest {

  override protected def beforeAll() {
    sys.props.put("config.resource", "common-dev.conf")
  }

  val appVal = "ok do ki"
  val propVal = "yes"
  val propVal2 = "no"
  val referenceUser = "importer"
  val applicationPwd = "imPortTsF"
  val protocol = "http"
  val mailLoglevel = "info"

  "The ServerConfig" should "have values for defined properties" in {
    assert(StringUtils.isNotBlank(adminMailRecipient))
    assert(StringUtils.isNotBlank(adminMailLoglevel.level))
  }

  it should "have their values from the common-dev.conf" in {
    config().getString(s"$configPath.propVal") should be(propVal)
    config().getString(s"$configPath.propVal2") should be(propVal2)
  }

  // strange problem with update
  it should "have their values from the sf.properties (system properties)" in {
    config().getString(mailFromProp) should be("servermails@screenfood.com")
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

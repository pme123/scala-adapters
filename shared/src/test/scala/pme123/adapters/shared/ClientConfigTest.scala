package pme123.adapters.shared

import play.api.libs.json.Json

class ClientConfigTest extends UnitTest {
  val clientConfig = ClientConfig("1231", JobConfig("dummyJob"))

  "ClientConfig" should "be marshaled and un-marshaled correctly" in {

    Json.toJson(clientConfig).validate[ClientConfig].get should be(clientConfig)
  }
}

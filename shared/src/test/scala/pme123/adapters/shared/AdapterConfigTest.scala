package pme123.adapters.shared

class AdapterConfigTest extends UnitTest {
  val config = ConfigObject(Map("root" -> ConfigList(List(
    ConfigInt(12)
    , ConfigString("hello")
  ))
  ))

  "Create a AdapterConfig" should "be easy" in {

    config.value
  }
}

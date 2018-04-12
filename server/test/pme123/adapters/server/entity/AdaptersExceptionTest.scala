package pme123.adapters.server.entity


class AdaptersExceptionTest
  extends UnitTest {


  "AllErrorMsgs for a AdaptersException" should "be formatted in an expected format" in {
    ConfigException("configException").allErrorMsgs should be(
      "configException [pme123.adapters.server.entity.AdaptersExceptionTest.$anonfun$new$1(AdaptersExceptionTest.scala:9)]"
    )
  }


  it should "be formatted also correct for Exceptions with causes" in {
    ConfigException("configException", Some(new Exception("firstCause", new Exception("secondCause")))).allErrorMsgs should be(
      """configException [pme123.adapters.server.entity.AdaptersExceptionTest.$anonfun$new$2(AdaptersExceptionTest.scala:16)]
      | - Cause: firstCause [pme123.adapters.server.entity.AdaptersExceptionTest.$anonfun$new$2(AdaptersExceptionTest.scala:16)]
      | - Cause: secondCause [pme123.adapters.server.entity.AdaptersExceptionTest.$anonfun$new$2(AdaptersExceptionTest.scala:16)]""".stripMargin

    )
  }
}

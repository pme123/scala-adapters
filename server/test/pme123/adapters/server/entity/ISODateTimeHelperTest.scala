package pme123.adapters.server.entity

class ISODateTimeHelperTest
  extends UnitTest
    with ISODateTimeHelper {


  "An ISO DateTime String" should "be the same after creating a LocalDateTime" in {
    val isoDate = "2018-01-13T12:33"
    toISODateTimeString(
      toLocalDateTime(isoDate)
    ) should be(isoDate)
  }
}

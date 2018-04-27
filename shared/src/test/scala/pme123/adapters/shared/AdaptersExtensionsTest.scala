package pme123.adapters.shared

import org.scalatest.DoNotDiscover

@DoNotDiscover
class AdaptersExtensionsTest extends UnitTest {
import AdaptersExtensions._

  val nullStr: String = null

  "null String" should "be blank" in {
    assert(nullStr.isBlank)
  }
  "empty String" should "be blank" in {
    assert("".isBlank)
  }
  "blank String" should "be blank" in {
    assert(" \n".isBlank)
  }
  "String" should "not be blank" in {
    assert(!"x".isBlank)
  }

  "null String" should "not be non-blank" in {
    assert(!nullStr.nonBlank)
  }
  "empty String" should "not be non-blank" in {
    assert(!"".nonBlank)
  }
  "blank String" should "not be non-blank" in {
    assert(!" \n".nonBlank)
  }
  "String" should "be non-blank" in {
    assert("x".nonBlank)
  }

}

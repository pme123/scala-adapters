package pme123.adapters.server.control

import org.scalatest._
import pme123.adapters.shared.Logger

/**
 * General Test Definition for ScalaTests
 */
trait AcceptanceSpec extends FeatureSpec
    with BeforeAndAfter
    with BeforeAndAfterAll
    with GivenWhenThen
    with Logger {
}

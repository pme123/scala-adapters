package pme123.adapters.server.entity

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import pme123.adapters.shared.Logger

trait UnitTest
  extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with Logger {

}

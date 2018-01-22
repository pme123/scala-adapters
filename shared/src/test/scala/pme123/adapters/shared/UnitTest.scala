package pme123.adapters.shared

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}

trait UnitTest
  extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with Logger {

}

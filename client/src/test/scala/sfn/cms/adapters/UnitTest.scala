package sfn.cms.adapters

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import sfn.cms.adapters.shared.Logger

trait UnitTest
  extends FlatSpec
      with Matchers
      with BeforeAndAfter
      with BeforeAndAfterAll
      with Logger {

  }

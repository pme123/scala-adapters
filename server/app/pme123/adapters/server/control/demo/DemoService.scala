package pme123.adapters.server.control.demo

import java.time.LocalDateTime

import pme123.adapters.server.entity.ISODateTimeHelper
import pme123.adapters.server.entity.demo.DemoAdapterContext.settings
import pme123.adapters.shared.Logger
import pme123.adapters.shared.demo.DemoResult

import scala.util.Random

object DemoService
  extends ISODateTimeHelper
    with Logger {

  info(s"Demo init $settings")

  lazy val results: Seq[DemoResult] =
    for {
      i <- 2 to 3
      k <- 1 to 5
    } yield DemoResult(s"Image Gallery $i - $k"
      , Left(s"https://www.gstatic.com/webp/gallery$i/$k.png")
      , toISODateTimeString(LocalDateTime.now().minusHours(Random.nextInt(100))))


}

package pme123.adapters.server.control.demo

import java.time.LocalDateTime

import pme123.adapters.server.entity.DateTimeHelper
import pme123.adapters.shared.demo.DemoResult

object DemoService
  extends DateTimeHelper {

  lazy val results: Seq[DemoResult] =
    for {
      i <- 2 to 3
      k <- 1 to 5
    } yield DemoResult(s"Image Gallery $i - $k"
      , s"https://www.gstatic.com/webp/gallery$i/$k.png"
      , localDateTimeStrFrom(LocalDateTime.now()))


}

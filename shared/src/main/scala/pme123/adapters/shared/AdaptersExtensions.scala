package pme123.adapters.shared

import scala.language.implicitConversions


// add useful extensions here
object AdaptersExtensions {

  class StringEx(val str: String) extends AnyVal {

    def isBlank: Boolean =
      Option(str).forall(_.trim.isEmpty)

    def nonBlank: Boolean =
      Option(str).exists(_.trim.nonEmpty)
  }

  implicit def toStringEx(input: String): StringEx = new StringEx(input)

}

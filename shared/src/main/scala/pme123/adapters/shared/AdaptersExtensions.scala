package pme123.adapters.shared

// add useful extensions here
object AdaptersExtensions {

  class StringEx(val str: String) extends AnyVal {

    def isBlank: Boolean =
      Option(str).forall(_.trim.isEmpty)

    def nonBlank: Boolean =
      Option(str).exists(_.trim.nonEmpty)
  }

  implicit def isBlank(input: String): StringEx = new StringEx(input)

}

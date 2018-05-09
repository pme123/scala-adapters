package pme123.adapters.shared

/**
  * general exception to mark a handled exception
  */
trait AdaptersException
  extends RuntimeException {
  def msg: String

  def cause: Option[Throwable] = None

  override def getMessage: String = msg

  override def getCause: Throwable = {
    cause.orNull
  }
}


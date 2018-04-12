package pme123.adapters.server.entity

/**
  * Marker trait for all internal Exceptions, that are handled.
  * Created by pascal.mengelt on 09.08.2016.
  */
trait AdaptersException
  extends RuntimeException {
  def msg: String

  def cause: Option[Throwable] = None

  override def getMessage: String = msg

  override def getCause: Throwable = {
    cause.orNull
  }

  lazy val allErrorMsgs: String = {
    def inner(throwable: Throwable, last: Throwable): String =
      if (throwable == null || throwable == last) ""
      else {
        val causeMsg = inner(throwable.getCause, throwable)
        throwable.getMessage + (if (causeMsg.nonEmpty) s"\n - Cause: $causeMsg" else "")
      }

    inner(this, null)
  }
}

case class JsonParseException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException {
}

case class ServiceException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException

case class ConfigException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException


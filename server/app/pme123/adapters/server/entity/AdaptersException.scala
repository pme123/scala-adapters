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
}

object AdaptersException {


}

case class JsonParseException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException {
}

case class ServiceException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException

case class ConfigException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException


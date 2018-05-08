package pme123.adapters.server.entity

import pme123.adapters.shared

/**
  * Marker trait for all internal Exceptions, that are handled.
  * Created by pascal.mengelt on 09.08.2016.
  */
trait AdaptersException
  extends shared.AdaptersException

case class JsonParseException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException {
}

case class ServiceException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException

case class ConfigException(msg: String, override val cause: Option[Throwable] = None)
  extends AdaptersException


package pme123.adapters.server.control.http

/**
  * Created by pascal.mengelt on 08.02.2016.
  */
trait WebException


case class WebBadStatusException(msg: String)
  extends RuntimeException(msg)
    with WebException

case class WebNotFoundException(msg: Option[String] = None)
  extends RuntimeException(msg.getOrElse("The Web Service was not found."))
    with WebException

case class WebAccessForbiddenException(msg: Option[String] = None)
  extends RuntimeException(msg.getOrElse("The Access to the Web Service was declined."))
    with WebException

case class WebNotAcceptableException(msg: Option[String] = None)
  extends RuntimeException(msg.getOrElse("The Request was not accepted (e.g. wrong header)."))
    with WebException

case class WebRequestException(cause: Throwable, msg: Option[String] = None)
  extends RuntimeException(msg.getOrElse("The Request to the Web Service failed, please see the cause."), cause)
    with WebException

case class WebDeserializeException(objStr: String, cause: Throwable)
  extends RuntimeException(s"The $objStr could not be deserialized: ${cause.getMessage}", cause)
    with WebException

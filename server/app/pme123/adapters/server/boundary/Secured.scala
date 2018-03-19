package pme123.adapters.server.boundary

import javax.inject.Inject

import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Security context that can be extended in order
  * to access the authenticated action-components.
  *
  * Support for:
  * - Basic HTTP authentication
  *
  * An implementor needs to implement:
  *
  * def isValidUser(user: String, pwd: String): Boolean
  *
  */
trait Secured {

  lazy val accessLogger = Logger("access-filter")

  def cc: ControllerComponents

  def accessControl: AccessControl

  /**
    * Request action builder that allows to add authentication
    * behavior to all specified actions.
    *
    * @return
    */
  def AuthenticatedAction: ActionBuilder[Request, AnyContent] = new ActionBuilder[Request, AnyContent] {

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      accessLogger.info(s"method=${request.method} uri=${request.uri} remote-address=${request.remoteAddress}")
      request.headers.get("Authorization").flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.filter { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if accessControl.isValidUser(u, p) => true
            case _ => false
          }
        }.map(_ => block(request))
      }.getOrElse {
        Future.successful(Results.Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured""""))
      }
    }

    def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    protected def executionContext: ExecutionContext = cc.executionContext
  }

  class UserRequest[A](val username: Option[String], request: Request[A]) extends WrappedRequest[A](request)

  class UserAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] with ActionTransformer[Request, UserRequest] {

    def transform[A](request: Request[A]): Future[UserRequest[A]] = Future.successful {
      new UserRequest(request.session.get("username"), request)
    }
  }

}


trait AccessControl {

  /**
    * A user is valid for authentication if:
    * - is the 'importer' user
    * - is a user with the 'admin' role
    *
    * @param user username
    * @param pwd  plain password
    * @return valid logged user for importing
    */
  def isValidUser(user: String, pwd: String): Boolean
}

// no access control needed
class NoAccessControl extends AccessControl {
  def isValidUser(user: String, pwd: String): Boolean = true
}
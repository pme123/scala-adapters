package pme123.adapters.server.control

import akka.actor.ActorRef
import akka.stream.Materializer
import pme123.adapters.server.entity.AdaptersContext
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.shared.{AdaptersContextProp, ProjectInfo}
import pme123.adapters.version.BuildInfo

import scala.concurrent.{ExecutionContext, Future}

trait JobProcess {

  implicit def mat: Materializer

  implicit def ec: ExecutionContext

  def email: String = adminMailRecipient

  def runJob(user: String)(implicit logService: LogService): Future[LogService]

  def createInfo(): ProjectInfo

  protected def createInfo(adapterVersion: String
                           , adapterProps: Seq[AdaptersContextProp]) =
    ProjectInfo(adapterVersion
      , BuildInfo.version
      , email
      , adapterProps
      , AdaptersContext.props
      , None
      , None
    )
}

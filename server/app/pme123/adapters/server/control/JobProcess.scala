package pme123.adapters.server.control

import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import play.api.libs.json.JsValue
import pme123.adapters.server.entity.{AdaptersContext, AdaptersSettings}
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.shared.{AdaptersContextProp, AdaptersContextProps, ProjectInfo}
import pme123.adapters.version.BuildInfo

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait JobProcess {

  implicit def mat: Materializer

  implicit def ec: ExecutionContext

  def email: String = adminMailRecipient

  implicit protected val timeout: Timeout = Timeout(1.second)

  def runJob(user: String, payload: Option[JsValue])(implicit logService: LogService
                                                     , jobActor: ActorRef): Future[LogService] =
    runJob(user)

  def runJob(user: String)
            (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService]

  def createInfo(): ProjectInfo

  protected def createInfo(projectVersion: String
                           , projectProps: AdaptersContextProps
                           , additionalVersions: Seq[AdaptersContextProp] = Nil
                           , additionalProps: Seq[AdaptersContextProps] = Nil
                           ) =
    ProjectInfo(projectVersion
      , BuildInfo.version
      , BuildInfo.builtAtString
      , email
      , projectProps
      , AdaptersContextProps(AdaptersSettings.configPath, AdaptersContext.props)
      , additionalVersions
      , additionalProps
    )
}


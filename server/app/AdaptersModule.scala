import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control._
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class AdaptersModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // framework
    LoggerConfig.factory = SLF4JLoggerFactory()
    bindActor[UserParentActor]("userParentActor")
    bindActorFactory[UserActor, UserActor.Factory]

    bindActor[JobActorSchedulers]("actorSchedulers")

  }
}

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.actor.{TestAdapterActor, UserActor, UserParentActor}
import pme123.adapters.server.control.actor.{UserActor, UserParentActor}
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class AdaptersModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    LoggerConfig.factory = SLF4JLoggerFactory()
    bindActor[TestAdapterActor]("adapterActor")
    bindActor[UserParentActor]("userParentActor")
    bindActorFactory[UserActor, UserActor.Factory]
  }
}

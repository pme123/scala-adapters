import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.demo.DemoJobFactory
import pme123.adapters.server.control.{JobActorFactory, JobActorScheduler, UserActor, UserParentActor}
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class AdaptersModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // demo
    bind(classOf[JobActorFactory]).to(classOf[DemoJobFactory])
    // framework
    LoggerConfig.factory = SLF4JLoggerFactory()
    bindActor[UserParentActor]("userParentActor")
    bindActorFactory[UserActor, UserActor.Factory]
    bind(classOf[JobActorScheduler]).asEagerSingleton()

  }
}

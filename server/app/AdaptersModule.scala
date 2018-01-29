import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.actor.{JobActorFactory, _}
import pme123.adapters.server.control.demo.{DemoJobActor, DemoJobFactory, DemoJobWithDefaultSchedulerActor, DemoJobWithoutSchedulerActor}
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class AdaptersModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // example

    bind(classOf[JobActorFactory]).to(classOf[DemoJobFactory])
    bindActor[DemoJobActor]("demoJob")
    bindActor[DemoJobWithDefaultSchedulerActor]("demoJobWithDefaultScheduler")
    bindActor[DemoJobWithoutSchedulerActor]("demoJobWithoutScheduler")
    // framework
    LoggerConfig.factory = SLF4JLoggerFactory()
    bindActor[UserParentActor]("userParentActor")
    bindActorFactory[UserActor, UserActor.Factory]
    bind(classOf[JobActorScheduler]).asEagerSingleton()

  }
}

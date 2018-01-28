import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.actor._
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class AdaptersModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    LoggerConfig.factory = SLF4JLoggerFactory()
    bindActor[UserParentActor]("userParentActor")
    bindActorFactory[UserActor, UserActor.Factory]
    // example
    bindActor[TestJobActor]("testJob")
    bindActor[TestJobWithDefaultSchedulerActor]("testJobWithDefaultScheduler")
    bindActor[TestJobWithoutSchedulerActor]("testJobWithoutScheduler")

  }
}

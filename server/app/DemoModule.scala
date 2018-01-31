import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.demo.DemoJobFactory
import pme123.adapters.server.control.{JobActorFactory, JobActorScheduler, UserActor, UserParentActor}
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class DemoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[JobActorFactory]).to(classOf[DemoJobFactory])
   }
}

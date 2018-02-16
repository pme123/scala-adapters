import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.server.control.demo.DemoJobFactory

class DemoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[JobActorFactory])
      .to(classOf[DemoJobFactory])
      .asEagerSingleton()
   }
}

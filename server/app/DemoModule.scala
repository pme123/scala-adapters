import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.boundary.{AccessControl, NoAccessControl}
import pme123.adapters.server.control.JobCreation
import pme123.adapters.server.control.demo.DemoJobCreation

class DemoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[JobCreation])
      .to(classOf[DemoJobCreation])
      .asEagerSingleton()

    bind(classOf[AccessControl])
      .to(classOf[NoAccessControl])

  }
}

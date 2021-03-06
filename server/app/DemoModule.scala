import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme123.adapters.server.boundary.NoAccessControl
import pme123.adapters.server.control.JobCreation
import pme123.adapters.server.control.demo.DemoJobCreation
import pme123.adapters.server.entity.demo.DemoAdapterContext
import pme123.adapters.shared.AccessControl

class DemoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // init the JobCreation factory
    bind(classOf[JobCreation])
      .to(classOf[DemoJobCreation])
      .asEagerSingleton()

    // you need to define the AccessControl
    bind(classOf[AccessControl])
      .to(classOf[NoAccessControl])

    DemoAdapterContext.logSettings

  }
}

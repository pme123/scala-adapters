package pme123.adapters.server.entity

import pme123.adapters.shared.ClientType.JOB_RESULTS

class ProjectConfigTest extends UnitTest {

  "A ProjectConfig" should "be created correctly" in {
    val projConfig = ProjectConfig("context", JOB_RESULTS,"/demo/ws", isDevMode = false)
    // values from constructor
    projConfig.context should be("context")
    projConfig.clientType should be(JOB_RESULTS)
    projConfig.websocketPath should be("/demo/ws")
    projConfig.isDevMode should be(false)

    // values from the reference.conf
    /*
      project.config {
          name = "scala-adapters"
          client.name = "DemoClient"
          page.title = "Demo Adapter"
          //style.name = "project"
          demo = true
     }
     */
    projConfig.projectName should be("scala-adapters")
    projConfig.clientName should be("DemoClient")
    projConfig.pageTitle should be("Demo Adapter")
    projConfig.styleName should be(None)
    projConfig.isDemo should be(true)
  }
}

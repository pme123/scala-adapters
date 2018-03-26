package pme123.adapters.server.boundary

import play.api.test.Helpers.await
import pme123.adapters.server.control.GuiceAcceptanceSpec
import pme123.adapters.shared.JobConfig

class JobCockpitControllerSpec
  extends GuiceAcceptanceSpec {

  "There must not be any ClientConfigs" in {
    val response = await(wsCall(routes.JobCockpitController.clientConfigs())
      .get())
    val jobConfigs = response.json.validate[Seq[JobConfig]]
    assert(jobConfigs.get.isEmpty)

  }
}

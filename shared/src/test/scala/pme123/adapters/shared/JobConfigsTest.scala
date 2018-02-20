package pme123.adapters.shared

import play.api.libs.json.Json

class JobConfigsTest extends UnitTest {

  "JobConfigs" should "be marshaled and un-marshaled correctly" in {
    val jobConfigs = JobConfigs(Seq(JobConfig("job1", Some(ScheduleConfig("01:00", 12)))
      , JobConfig("job2")))

    Json.toJson(jobConfigs).validate[JobConfigs].get should be(jobConfigs)
  }

  "JobConfig" should "create its webPath correctly" in {
    JobConfig("job2").webPath should be("/job2")
    JobConfig("job2", subWebPath = "/test/path").webPath should be("/job2/test/path")
  }
}

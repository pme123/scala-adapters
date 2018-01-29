package pme123.adapters.shared

import play.api.libs.json.Json

class JobConfigsTest extends UnitTest {

  "JobConfigs" should "be marshaled and un-marshaled correctly" in {
    val jobConfigs = JobConfigs(Map("job1" -> JobConfig("job1", Some(ScheduleConfig("01:00", 12)))
    , "job2" -> JobConfig("job2")))

    Json.toJson(jobConfigs).validate[JobConfigs].get should be(jobConfigs)
  }
}

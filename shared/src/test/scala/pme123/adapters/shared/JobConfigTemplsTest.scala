package pme123.adapters.shared

import play.api.libs.json.Json

class JobConfigTemplsTest extends UnitTest {

  "JobConfigs" should "be marshaled and un-marshaled correctly" in {
    val jobConfigTempls = JobConfigTempls(Map("job1" -> JobConfigTempl("job1", Some(ScheduleConfig("01:00", 12)))
    , "job2" -> JobConfigTempl("job2")))

    Json.toJson(jobConfigTempls).validate[JobConfigTempls].get should be(jobConfigTempls)
  }
}

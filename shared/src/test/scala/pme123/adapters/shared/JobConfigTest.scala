package pme123.adapters.shared

class JobConfigTest extends UnitTest {

  "JobConfig" should "create its webPath correctly" in {
    JobConfig("job2").webPath should be("/job2")
    JobConfig("job2", subWebPath = "/test/path").webPath should be("/job2/test/path")
  }
}

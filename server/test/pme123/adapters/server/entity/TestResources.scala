package pme123.adapters.server.entity

import java.time.LocalDate

import com.typesafe.config.Config
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.AdaptersSettings.jobConfigsProp
import pme123.adapters.shared.JobConfig

trait TestResources {

  val testYear = 2017
  val testDay = 4
  val testMonth = 5
  val testNow: LocalDate = LocalDate.of(testYear, testMonth, testDay)

  def getJobConfig(index: Int): Config = projectConfig.getConfigList(jobConfigsProp).get(index)

  val jobConfigDefault: JobConfig = JobConfigCreator(getJobConfig(1), timezoneID).create()._2
}

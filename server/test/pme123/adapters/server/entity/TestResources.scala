package pme123.adapters.server.entity

import java.time.LocalDate

import com.typesafe.config.Config
import pme123.adapters.server.entity.AdaptersContext.settings._
import pme123.adapters.server.entity.AdaptersSettings.jobConfigTemplsProp
import pme123.adapters.shared.JobConfigTempl

trait TestResources {

  val testYear = 2017
  val testDay = 4
  val testMonth = 5
  val testNow: LocalDate = LocalDate.of(testYear, testMonth, testDay)

  def getJobConfig(index: Int): Config = projectConfig.getConfigList(jobConfigTemplsProp).get(index)

  val jobConfigTemplDefault: JobConfigTempl = JobConfigCreator(getJobConfig(1), timezoneID).create()._2
}

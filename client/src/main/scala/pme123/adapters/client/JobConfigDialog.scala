package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.JobConfig

private[client] case class JobConfigDialog(context: String)
  extends ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal">
      {ServerServices(context).jobConfigs().bind}{//
      detailHeader.bind}{//
      jobsTable.bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Registered Jobs
  </div>

  @dom
  private def jobsTable = {
    val jobs = UIStore.uiState.allJobs.bind
    <div class="content">
      <table class="ui padded table">
        <thead>
          <tr>
            <th>Job Ident</th>
            <th>Schedule First Time</th>
            <th>Schedule First Week Day</th>
            <th>Schedule Interval in Min.</th>
            <th>Sub Webpath</th>
          </tr>
        </thead>
        <tbody>
          {Constants(jobs.map(propRow): _*)
          .map(_.bind)}
        </tbody>
      </table>
    </div>
  }

  @dom
  private def propRow(jobConfig: JobConfig) =
    <tr>
      <td class="two wide">
        {jobConfig.jobIdent}
      </td>
      <td class="three wide">
        {jobConfig.jobSchedule.map(_.firstTime).getOrElse("-")}
      </td>
      <td class="three wide">
        {jobConfig.jobSchedule.map(_.firstWeekDay.getOrElse("-")).getOrElse("-")}
      </td>
      <td class="two wide">
        {jobConfig.jobSchedule.map(_.intervalInMin.toString).getOrElse("-")}
      </td>
      <td class="six wide">
        {jobConfig.subWebPath}
      </td>
    </tr>
  

}

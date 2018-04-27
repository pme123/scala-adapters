package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.{AdaptersContextProp, ProjectInfo}

private[client] case class ProjectInfoDialog(projectInfo: ProjectInfo)
  extends ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal detailDialog">
      {detailHeader.bind}{//
      infoList.bind}{//
      versionList.bind}{//
      propTable("Project Properties", projectInfo.adapterProps).bind}{//
      propTable("Common Properties", projectInfo.commonProps).bind}
      </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Project Infos
  </div>

  private def infoList = {
    propTable("Main Info", Seq(
      AdaptersContextProp("Notification email", projectInfo.adminMailRecipient)
      , AdaptersContextProp("Last execution", projectInfo.lastExecution
        .map(jsLocalDateTime)
        .getOrElse("Never")
      )) ++
      projectInfo.schedulerInfo.map(si =>
        Seq(AdaptersContextProp("Next execution time", jsLocalDateTime(si.nextExecution))
          , AdaptersContextProp("First day of week", si.firstWeekday)
          , AdaptersContextProp("Execution Period in minutes", si.periodInMin.toString)
        )
      ).getOrElse(Nil)
    )
  }

  private def versionList = {
    propTable("Versions", Seq(
      AdaptersContextProp("This project", projectInfo.projectVersion)
      , AdaptersContextProp("Build time", projectInfo.buildTime)
      , AdaptersContextProp("scala-adapters", projectInfo.adaptersVersion)
    ) ++ projectInfo.additionalVersions)
  }

  @dom
  private def propTable(header: String, props: Seq[AdaptersContextProp]) =
    <div class="content">
      <div class="header">
        {header}
      </div>
      <table class="ui basic table">
        <tbody>
          {Constants(props.map(propRow): _*)
          .map(_.bind)}
        </tbody>
      </table>
    </div>

  @dom
  private def propRow(prop: AdaptersContextProp) =
    <tr>
      <td class="four wide">
        {prop.key}
      </td> <td class="twelve wide">
      {prop.value}
    </td>
    </tr>

}

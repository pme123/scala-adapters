package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.{AdapterInfo, AdaptersContextProp, SchedulerInfo}

private[client] case class AdapterInfoDialog(adapterInfo: AdapterInfo, uiState: UIState)
  extends UIStore
    with ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal">
      {detailHeader.bind}{//
      infoList.bind}{//
      versionList.bind}{//
      propTable("Adapter Properties", adapterInfo.adapterProps).bind}{//
      propTable("Common Properties", adapterInfo.commonProps).bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Adapter Infos
  </div>

  private def infoList = {
    propTable("Main Info", Seq(
      AdaptersContextProp("Notification email", adapterInfo.adminMailRecipient)
      , AdaptersContextProp("Last execution", adapterInfo.lastExecution
        .map(jsLocalDateTime)
        .getOrElse("Never")
      )) ++
      adapterInfo.schedulerInfo.map(si =>
        Seq(AdaptersContextProp("Next execution time", jsLocalDateTime(si.nextExecution))
          , AdaptersContextProp("First day of week", si.firstWeekday)
          , AdaptersContextProp("Execution Period in minutes", si.periodInMin.toString)
        )
      ).getOrElse(Nil)
    )
  }

  private def versionList = {
    propTable("Versions", Seq(
      AdaptersContextProp("Adapter", adapterInfo.adapterVersion)
      , AdaptersContextProp("Common Adapter", adapterInfo.commonVersion)
    ))
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

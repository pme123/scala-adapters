package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.client.UIStore._
import pme123.adapters.shared.ClientConfig

private[client] object ClientConfigDialog
  extends IntellijImplicits {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal detailDialog">
      {ServerServices.clientConfigs().bind}{//
      detailHeader.bind}{//
      clientsTable.bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Registered Clients
  </div>

  @dom
  private def clientsTable = {
    <div class="content">
      <table class="ui padded table">
        <thead>
          <tr>
            <th>Client Id</th>
            <th>Job Ident</th>
            <th>Job Sub-Webpath</th>
            <th>Result Count</th>
            <th>Result Filter</th>
          </tr>
        </thead>
        <tbody>
          {for (cc <- uiState.allClients) yield propRow(cc).bind}
        </tbody>
      </table>
    </div>
  }

  @dom
  private def propRow(clientConfig: ClientConfig) =
    <tr>
      <td class="two wide">
        {clientConfig.requestIdent}
      </td>
      <td class="two wide">
        {clientConfig.jobConfig.jobIdent}
      </td>
      <td class="three wide">
        {clientConfig.jobConfig.subWebPath}
      </td>
      <td class="one wide">
        {s"${clientConfig.resultCount}"}
      </td>
      <td class="eight wide">
        {clientConfig.resultFilter.getOrElse("-")}
      </td>
    </tr>

}

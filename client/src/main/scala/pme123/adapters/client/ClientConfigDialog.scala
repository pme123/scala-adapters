package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.ClientConfig
import UIStore._

private[client] case class ClientConfigDialog(context: String)
  extends IntellijImplicits {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal">
      {ServerServices(context).clientConfigs().bind}{//
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
    val clients = uiState.allClients.bind
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
          {Constants(clients.map(propRow): _*)
          .map(_.bind)}
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

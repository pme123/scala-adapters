package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.ClientConfig
import pme123.adapters.shared.JobConfig.JobIdent

private[client] case class ClientConfigDialog(uiState: UIState
                                              , context: String
                                              , jobIdent: JobIdent)
  extends UIStore
    with IntellijImplicits {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal">
      {ServerServices(uiState, context).clientConfigs(jobIdent).bind}{//
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
            <th>Info</th>
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
      <td class="four wide">
        {clientConfig.requestIdent}
      </td>
      <td class="twelve wide">
        {clientConfig.info}
      </td>
    </tr>

}

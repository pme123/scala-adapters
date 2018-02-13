package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.JsValue
import pme123.adapters.shared.JobConfig.JobIdent

private[client] case class LastResultDialog(uiState: UIState
                                            , context: String
                                            , jobIdent: JobIdent)
  extends UIStore {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal">
      {detailHeader.bind}{//
      resultsTable.bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Last Result (as raw JSON)
  </div>

  @dom
  private def resultsTable = {
    val lastResults = uiState.lastResults.bind
    <div class="content">
      <table class="ui padded table">
        <thead>
          <tr>
            <th>Result as JSON</th>
          </tr>
        </thead>
        <tbody>
          {Constants(lastResults.map(resultRow): _*)
          .map(_.bind)}
        </tbody>
      </table>
    </div>
  }

  @dom
  private def resultRow(result: JsValue) =
    <tr>
      <td class="sixteen wide">
        {result.toString}
      </td>
    </tr>

}

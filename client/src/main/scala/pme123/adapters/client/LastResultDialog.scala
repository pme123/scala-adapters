package pme123.adapters.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import play.api.libs.json.JsValue

private[client] case class LastResultDialog()
  extends ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal detailDialog">
      {detailHeader.bind}{//
      resultsTable.bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    Last Result
  </div>

  @dom
  private def resultsTable = {
    <div class="content">
      <table class="ui padded table">
        <thead>
          <tr>
            <th>Result as JSON</th>
          </tr>
        </thead>
        <tbody>
          {for (lr <- UIStore.uiState.lastResults) yield resultRow(lr).bind}
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

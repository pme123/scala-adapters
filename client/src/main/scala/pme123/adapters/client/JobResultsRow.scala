package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement

case class JobResultsHeader(fields: Seq[String]) {

  @dom
  private[client] def headerRow =
    <thead>
      <tr>
        {Constants(fields.map(headerCell): _*)
        .map(_.bind)}
      </tr>
    </thead>

  @dom
  private def headerCell(value: String): Binding[HTMLElement] =
    <td>
      {value}
    </td>
}

case class JobResultsRow(values: Seq[String]) {

  @dom
  private[client] lazy val resultRow =

    <tr>
      {Constants(values.map(resultCell): _*)
      .map(_.bind)}
    </tr>

  @dom
  private def resultCell(value: String): Binding[HTMLElement] =
    <td>
      {value}
    </td>
}
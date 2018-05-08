package pme123.adapters.client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared._

private[client] case class LogEntryDetailDialog(logEntry: LogEntry)
  extends ClientUtils {

  // 1. level of abstraction
  // **************************
  @dom
  private[client] def showDetail(): Binding[HTMLElement] =
    <div class="ui modal detailDialog">
      <div class="content">
        {jsLocalDateTime(logEntry.timestamp)}
      </div>{detailHeader.bind}{//
      detailContent.bind}
    </div>

  // 2. level of abstraction
  // **************************

  @dom
  private def detailHeader = <div class="header">
    {logLevelIcon(logEntry).bind}{//
    s"Log Entry ${logEntry.msg}"}
  </div>

  @dom
  protected def logLevelMessage(entry: LogEntry, divClass: String = "content"): Binding[HTMLElement] =
    <div class="header">
      {entry.msg}
    </div>

  @dom
  private def detailContent =
    <div class="content">

      {/*val entryStrings: immutable.Seq[String] = logEntry.detail.toList.flatMap(_.split("\n"))
    Constants(entryStrings.map(detailRow): _*).map(_.bind)*/
      Constants(logEntry.detail.toList.map(detail): _*).map(_.bind)}

    </div>

  @dom
  private def detailRow(row: String): Binding[HTMLElement] =
    <div class="content">
      {row}
    </div>

  @dom
  private def detail(detail: String): Binding[HTMLElement] =
    <pre>
      {detail}
    </pre>

}

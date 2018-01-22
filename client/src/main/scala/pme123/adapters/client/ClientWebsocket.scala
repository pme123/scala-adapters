package pme123.adapters.client

import org.scalajs.dom.raw._
import org.scalajs.dom.{document, window}
import play.api.libs.json.{JsError, JsSuccess, Json}
import pme123.adapters.shared.LogReport
import pme123.adapters.shared.{AdapterMsg, RunAdapter, RunStarted, _}

import scala.scalajs.js.timers.setTimeout

case class ClientWebsocket(uiState: UIState, project: String, adapter: String)
  extends UIStore {

  private lazy val wsURL = s"${window.location.protocol.replace("http","ws")}//${window.location.host}/$project/ws/$adapter"

  lazy val socket = new WebSocket(wsURL)

  def connectWS() {

    socket.onmessage = {
      (e: MessageEvent) =>
        val message = Json.parse(e.data.toString)
        message.validate[AdapterMsg] match {
          case JsSuccess(AdapterRunning(logReport), _) =>
            changeIsRunning(true)
            newLogReport(logReport)
          case JsSuccess(AdapterNotRunning(logReport), _) =>
            changeIsRunning(false)
            logReport.foreach { lr =>
              changeLastLogLevel(lr)
              newLogReport(lr)
            }
          case JsSuccess(LogEntryMsg(le), _) =>
            newLogEntry(le)
          case JsSuccess(RunStarted, _) =>
            changeIsRunning(true)
          case JsSuccess(RunFinished(logReport), _) =>
            changeIsRunning(false)
            changeLastLogLevel(logReport)
          case JsSuccess(adapterInfo: AdapterInfo, _) =>
            changeAdapterInfo(adapterInfo)
          case JsSuccess(other, _) =>
            info(s"Other message: $other")
          case JsError(errors) =>
            errors.foreach(e => error(e.toString))
        }
    }
    socket.onerror = { (e: ErrorEvent) =>
      error(s"exception with websocket: ${e.message}!")
      socket.close(0, e.message)
    }
    socket.onopen = { (_: Event) =>
      info("websocket open!")
      clearLogData()
    }
    socket.onclose = { (e: CloseEvent) =>
      info("closed socket" + e.reason)
      setTimeout(1000) {
        connectWS() // try to reconnect automatically
      }
    }
  }

  def runAdapter() {
    info("run Adapter")
    socket.send(Json.toJson(RunAdapter()).toString())
  }

  private def newLogReport(logReport: LogReport) {
    addLogReport(logReport)
    scrollDown(logReport.logEntries.size)
  }

  private def newLogEntry(logEntry: LogEntry) {
    addLogEntry(logEntry)

    scrollDown()
  }

  private def scrollDown(count:Int = 1) {
    val objDiv = document.getElementById("log-panel")
    objDiv.scrollTop = objDiv.scrollHeight - count * 20
  }
}

package pme123.adapters.client

import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw._
import org.scalajs.dom.window
import play.api.libs.json.{JsError, JsSuccess, Json}
import pme123.adapters.shared.JobConfig.JobIdent
import pme123.adapters.shared.{AdapterMsg, RunJob, RunStarted, _}

import scala.scalajs.js.timers.setTimeout

case class ClientWebsocket(uiState: UIState
                           , context: String)
  extends UIStore {

  private lazy val wsURL = s"${window.location.protocol.replace("http", "ws")}//${window.location.host}$context/ws"

  private val webSocket: Var[Option[WebSocket]] = Var(None)
  private val reconnectWSCode = 3001


  def connectWS(selJobIdent: Option[JobIdent]) {
    closeWS()
    selJobIdent.foreach { ji =>
      val path = s"$wsURL/$ji"
      val socket = new WebSocket(path)
      webSocket.value = Some(socket)
      info(s"Connect to Websocket: $path")
      socket.onmessage = {
        (e: MessageEvent) =>
          val message = Json.parse(e.data.toString)
          message.validate[AdapterMsg] match {
            case JsSuccess(AdapterRunning(logReport), _) =>
              changeIsRunning(true)
              addLogReport(logReport)
            case JsSuccess(AdapterNotRunning(logReport), _) =>
              changeIsRunning(false)
              logReport.foreach { lr =>
                changeLastLogLevel(lr)
                addLogReport(lr)
              }
            case JsSuccess(LogEntryMsg(le), _) =>
              addLogEntry(le)
            case JsSuccess(RunStarted, _) =>
              changeIsRunning(true)
            case JsSuccess(RunFinished(logReport), _) =>
              changeIsRunning(false)
              changeLastLogLevel(logReport)
            case JsSuccess(adapterInfo: ProjectInfo, _) =>
              changeAdapterInfo(adapterInfo)
            case JsSuccess(GenericResult(payload), _) =>
              addLastResult(payload)
            case JsSuccess(GenericResults(payload), _) =>
              replaceLastResults(payload)
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
        if (e.code != reconnectWSCode) {
          setTimeout(1000) {
            connectWS(selJobIdent) // try to reconnect automatically
          }
        }
      }
    }
  }

  def runAdapter() {
    info("run Adapter")
    webSocket.value
      .foreach(_.send(Json.toJson(RunJob())
        .toString()))
  }

  def closeWS(): Unit = {
    webSocket.value.foreach(_.close(reconnectWSCode, ": Reconnect for different configuration."))
  }

}

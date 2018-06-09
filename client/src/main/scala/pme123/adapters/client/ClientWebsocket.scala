package pme123.adapters.client

import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import pme123.adapters.shared.{AdapterMsg, RunJob, RunStarted, _}

import scala.scalajs.js.timers.setTimeout

object ClientWebsocket
  extends ClientUtils {

  private lazy val wsURL =
    s"${
      window.location.protocol
        .replace("http", "ws")
    }//${window.location.host}${UIStore.uiState.webContext.value}/ws"

  private val webSocket: Var[Option[WebSocket]] = Var(None)
  private val reconnectWSCode = 3001


  def connectWS() {
    closeWS()
    val webPath = UIStore.uiState.webPath.value
    val path = s"$wsURL$webPath"
    val socket = new WebSocket(path)
    webSocket.value = Some(socket)
    info(s"Connect to Websocket: $path")
    socket.onmessage = {
      e: MessageEvent =>
        val message = Json.parse(e.data.toString)
        message.validate[AdapterMsg] match {
          case JsSuccess(AdapterRunning(logReport), _) =>
            UIStore.changeIsRunning(true)
            UIStore.addLogReport(logReport)
          case JsSuccess(AdapterNotRunning(logReport), _) =>
            UIStore.changeIsRunning(false)
            logReport.foreach { lr =>
              UIStore.changeLastLogLevel(lr)
              UIStore.addLogReport(lr)
            }
          case JsSuccess(LogEntryMsg(le), _) =>
            UIStore.addLogEntry(le)
          case JsSuccess(RunStarted, _) =>
            UIStore.changeIsRunning(true)
          case JsSuccess(RunFinished(logReport), _) =>
            UIStore.changeIsRunning(false)
            UIStore.changeLastLogLevel(logReport)
          case JsSuccess(adapterInfo: ProjectInfo, _) =>
            UIStore.changeProjectInfo(adapterInfo)
          case JsSuccess(ClientConfigMsg(clientConfig), _) =>
            UIStore.changeSelectedClientConfig(Some(clientConfig))
          case JsSuccess(GenericResult(payload, append), _) =>
            UIStore.replaceLastResult(payload, append)
          case JsSuccess(GenericResults(payload, append), _) =>
            UIStore.replaceLastResults(payload, append)
          case JsSuccess(other, _) =>
            info(s"Other message: $other")
          case JsError(errors) =>
            errors.foreach(e => error(e.toString))
        }
    }
    socket.onerror = { e: Event =>
      val ee = e.asInstanceOf[ErrorEvent]
      error(s"exception with websocket: ${ee.message}!")
      socket.close(0, ee.message)
    }
    socket.onopen = { _: Event =>
      info("websocket open!")
      UIStore.clearLogData()
    }
    socket.onclose = { e: CloseEvent =>
      info("closed socket" + e.reason)
      if (e.code != reconnectWSCode) {
        setTimeout(1000) {
          connectWS() // try to reconnect automatically
        }
      }
    }
  }

  def runAdapter() {
    runAdapter(None)
  }

  def runAdapter(payload: Option[JsValue]) {
    info("run Adapter")
    webSocket.value
      .foreach(_.send(Json.toJson(RunJob(payload = payload))
        .toString()))
  }

  def closeWS(): Unit = {
    webSocket.value.foreach(_.close(reconnectWSCode, ": Reconnect for different configuration."))
  }

}

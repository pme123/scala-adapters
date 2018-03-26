package pme123.adapters.server.boundary

import java.util.concurrent.ArrayBlockingQueue

import org.awaitility.Awaitility.await
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Json}
import play.api.test.Helpers
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import play.shaded.ahc.org.asynchttpclient.ws.WebSocket
import pme123.adapters.server.control.{GuiceAcceptanceSpec, WebSocketClient}
import pme123.adapters.shared._
import pme123.adapters.shared.demo.DemoJobs

import scala.compat.java8.FutureConverters
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class JobCockpitControllerSpec
  extends GuiceAcceptanceSpec
    with ScalaFutures {

  implicit val ec: ExecutionContext = inject[ExecutionContext]

  "There must not be any ClientConfigs" in {
    assert(clientConfigs().isEmpty)
  }

  "There must be 3 static JobConfigs (demoJobs)" in {
    assert(jobConfigs().size == 3)
  }

  "Creating a WebSocket should return several AdapterMsgs over the WebSocket" in {
    val queue = new ArrayBlockingQueue[String](10)

    val f = createWebSocket(queue)

    whenReady(f, timeout = Timeout(1.second)) { webSocket =>
      await().until(() => webSocket.isOpen && queue.peek() != null)

      checkRunning(queue.take())
      checkClientConfig(queue.take())
      checkProjectInfo(queue.take())
      assert(queue.isEmpty)
      webSocket.close()
    }
  }

  "Creating several WebSockets should create the same number of ClientConfigs" in {
    val result: Future[Seq[WebSocket]] = Future.sequence((1 to 10).map(_ => createWebSocket(new ArrayBlockingQueue[String](10))))

    whenReady(result, timeout = Timeout(1.second)) { webSockets =>

      assert(webSockets.size == 10)
      assert(clientConfigs().size == 10)
      assert(clientConfigs().head.jobConfig.jobIdent == DemoJobs.demoJobIdent)

      // after closing all
      webSockets.foreach(_.close())
      assert(clientConfigs().isEmpty)
    }
  }

  "Creating a WebSocket with a dynamic JobConfig should return several AdapterMsgs over the WebSocket" in {
    val queue = new ArrayBlockingQueue[String](10)

    val filter = "subject%3D123"
    val count = 3
    val subWebPath = "/dynamicPath"
    val f = createWebSocket(queue, s"/jobProcess$subWebPath?resultCount=$count&resultFilter=$filter")

    whenReady(f, timeout = Timeout(1.second)) { webSocket =>
      await().until(() => webSocket.isOpen && queue.peek() != null)

      checkRunning(queue.take())
      val clientConfig = checkClientConfig(queue.take())
      assert(clientConfig.jobConfig.subWebPath == subWebPath)
      assert(clientConfig.resultCount == count)
      assert(clientConfig.resultFilter.contains("subject=123"))
      checkProjectInfo(queue.take())
      assert(queue.isEmpty)
      // another JobConfig should be registered
      assert(jobConfigs().size == 4)
      webSocket.close()
    }
  }

  private def createWebSocket(queue: ArrayBlockingQueue[String], path: String = "/demoJob") = {
    val myPublicAddress = s"localhost:$port"
    val serverURL = s"ws://$myPublicAddress/ws$path"

    val asyncHttpClient: AsyncHttpClient = wsClient.underlying[AsyncHttpClient]
    val webSocketClient = new WebSocketClient(asyncHttpClient)
    val origin = serverURL
    val listener = new WebSocketClient.LoggingListener(message => queue.put(message))
    val completionStage = webSocketClient.call(serverURL, origin, listener)
    FutureConverters.toScala(completionStage)
  }

  private def clientConfigs() = {
    val response = Helpers.await(wsCall(routes.JobCockpitController.clientConfigs())
      .get())
    val clientConfigs = response.json.validate[Seq[ClientConfig]]
    clientConfigs.get
  }

  private def jobConfigs() = {
    val response = Helpers.await(wsCall(routes.JobCockpitController.jobConfigs())
      .get())
    val jobConfigs = response.json.validate[Seq[JobConfig]]
    jobConfigs.get
  }

  private def checkRunning(msg: String) {
    val json: JsValue = Json.parse(msg)
    json.validate[AdapterMsg] match {
      case JsSuccess(AdapterNotRunning(None), _) => // ok
      case other => fail(s"Unexpected result: $other")
    }
  }

  private def checkClientConfig(clientConfigMsg: String) = {
    val json: JsValue = Json.parse(clientConfigMsg)
    val clientConfig = json.validate[AdapterMsg] match {
      case JsSuccess(ClientConfigMsg(clConfig), _) =>
        assert(clConfig.jobConfig.jobIdent == DemoJobs.demoJobIdent)
        clConfig
      case other => fail(s"Unexpected result: $other")
    }
    // the service should now return the ClientConfig as well
    assert(clientConfigs().size == 1)
    assert(clientConfigs().head.jobConfig.jobIdent == DemoJobs.demoJobIdent)
    clientConfig
  }

  private def checkProjectInfo(msg: String) {
    val json: JsValue = Json.parse(msg)
    json.validate[AdapterMsg] match {
      case JsSuccess(_:ProjectInfo, _) => // ok
      case other => fail(s"Unexpected result: $other")
    }
  }

}

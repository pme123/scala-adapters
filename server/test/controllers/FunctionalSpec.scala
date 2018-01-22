package controllers

import java.util.concurrent.ArrayBlockingQueue

import org.awaitility.Awaitility._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.test.{Helpers, TestServer, WsTestClient}
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import pme123.adapters.shared.LogLevel.INFO
import pme123.adapters.shared.{AdapterMsg, RunAdapter, _}

import scala.compat.java8.FutureConverters
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

// Original see here: https://github.com/playframework/play-scala-websocket-example
class FunctionalSpec extends PlaySpec with ScalaFutures {

  "SampleController" ignore {

    "reject a websocket flow if the origin is set incorrectly" in WsTestClient.withClient { client =>

      // Pick a non standard port that will fail the (somewhat contrived) origin check...
      lazy val port: Int = 31337
      val app = new GuiceApplicationBuilder().build()
      Helpers.running(TestServer(port, app)) {
        val myPublicAddress = s"localhost:$port"
        val serverURL = s"ws://$myPublicAddress/ws"

        val asyncHttpClient: AsyncHttpClient = client.underlying[AsyncHttpClient]
        val webSocketClient = new WebSocketClient(asyncHttpClient)
        try {
          val origin = "ws://client.com/ws"
          val listener = new WebSocketClient.LoggingListener(message => println(message))
          val completionStage = webSocketClient.call(serverURL, origin, listener)
          val f = FutureConverters.toScala(completionStage)
          Await.result(f, atMost = 1000 millis)
          listener.getThrowable mustBe a[IllegalStateException]
        } catch {
          case e: IllegalStateException =>
            e mustBe an [IllegalStateException]

          case e: java.util.concurrent.ExecutionException =>
            val foo = e.getCause
            foo mustBe an [IllegalStateException]
        }
      }
    }

    "accept a websocket flow if the origin is set correctly" in WsTestClient.withClient { client =>
      lazy val port: Int = Helpers.testServerPort
      val app = new GuiceApplicationBuilder().build()
      Helpers.running(TestServer(port, app)) {
        val myPublicAddress = s"localhost:$port"
        val serverURL = s"ws://$myPublicAddress/ws"

        val asyncHttpClient: AsyncHttpClient = client.underlying[AsyncHttpClient]
        val webSocketClient = new WebSocketClient(asyncHttpClient)
        val queue = new ArrayBlockingQueue[String](10)
        val origin = serverURL
        val listener = new WebSocketClient.LoggingListener(message => queue.put(message))
        val completionStage = webSocketClient.call(serverURL, origin, listener)
        val f = FutureConverters.toScala(completionStage)

        // Test we can get good output from the websocket
        whenReady(f, timeout = Timeout(1 second)) { webSocket =>
          await().until(() => webSocket.isOpen && queue.peek() != null)
          val input: String = queue.take()
          val json:JsValue = Json.parse(input)
          json.validate[AdapterMsg] match {
            case JsSuccess(AdapterNotRunning(None), _) => // ok
            case other => fail(s"Unexpected result: $other")
          }
          // run Adapter
          webSocket.sendMessage(Json.toJson(RunAdapter("tester")).toString())
          Json.parse(queue.take()).validate[AdapterMsg] match {
            case JsSuccess(LogEntryMsg(LogEntry(INFO, msg, _, _)), _) => // ok
              msg must startWith("Demo Adapter Process started at ")
            case other => fail(s"Unexpected result: $other")
          }
        }
      }
    }
  }
}

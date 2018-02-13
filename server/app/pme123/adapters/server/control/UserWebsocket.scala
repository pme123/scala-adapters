package pme123.adapters.server.control

import akka.actor.{Actor, ActorRef}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.{Done, NotUsed}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import pme123.adapters.shared._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait UserWebsocket
  extends Actor
    with Logger {
  implicit def mat: Materializer
  implicit def ec: ExecutionContext

  private lazy val (hubSink, hubSource) = MergeHub.source[JsValue](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  def wsActor: ActorRef = {
    // We convert everything to JsValue so we get a single stream for the websocket.
    // As all messages are AdapterMessages we only need one Source.
    val adapterActorSource = Source.actorRef(Int.MaxValue, OverflowStrategy.fail)
    // Set up a complete runnable graph from the adapter source to the hub's sink
    Flow[AdapterMsg]
      // send every minute a KeepAliveMsg - as with akka-http there is an idle-timeout
      .keepAlive(1.minute, () => KeepAliveMsg)
      .map(Json.toJson[AdapterMsg])
      .to(hubSink)
      .runWith(adapterActorSource)
  }

  /**
    * Generates a flow that can be used by the websocket.
    *
    * @return the flow of JSON
    */
  def websocketFlow(jobActor: ActorRef): Flow[JsValue, JsValue, NotUsed] = {
    // Put the source and sink together to make a flow of hub source as output (aggregating all
    // AdapterMsgs as JSON to the browser) and the actor as the sink (receiving any JSON messages
    // from the browse), using a coupled sink and source.
    Flow.fromSinkAndSourceCoupled(jsonSink(jobActor), hubSource)
      .watchTermination() {
        (_, termination) =>
          // When the flow shuts down, make sure this actor also stops.
          termination.foreach(_ => context.stop(self))
          NotUsed
      }
  }

  def jsonSink(jobActor: ActorRef): Sink[JsValue, Future[Done]] = Sink.foreach { json =>
    // When the initiator runs the Adapter
    json.validate[AdapterMsg] match {
      case JsSuccess(runAdapter: RunJob, _) =>
        jobActor ! runAdapter
      case JsSuccess(other, _) =>
        warn(s"Unexpected message received: $other")
      case JsError(errors) =>
        error("Other than RunAdapter: " + errors.toString())
    }
  }
}


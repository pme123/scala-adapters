package pme123.adapters.server.control

import javax.inject._

import akka.actor._
import akka.event.{LogMarker, MarkerLoggingAdapter}
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import akka.{Done, NotUsed}
import com.google.inject.assistedinject.Assisted
import play.api.libs.json._
import pme123.adapters.server.control.JobActor.{SubscribeAdapter, UnSubscribeAdapter}
import pme123.adapters.server.control.UserActor.CreateAdapter
import pme123.adapters.shared.{AdapterMsg, KeepAliveMsg, RunJob}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Creates a initiator actor that sets up the websocket stream.  Although it's not required,
  * having an actor manage the stream helps with lifecycle and monitoring, and also helps
  * with dependency injection through the AkkaGuiceSupport trait.
  *
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  *
  * @param adapterActor the actor responsible for the Adapter process
  * @param ec           implicit CPU bound execution context.
  */
class UserActor @Inject()(@Assisted id: String
                          , @Assisted adapterActor: ActorRef)
                         (implicit mat: Materializer, ec: ExecutionContext)
  extends Actor {

  // Useful way to mark out individual actors with websocket request context information...
  private val marker = LogMarker(name = self.path.name)
  implicit private val log: MarkerLoggingAdapter = akka.event.Logging.withMarker(context.system, this.getClass)
  implicit private val timeout: Timeout = Timeout(50.millis)

  private var clientId = "NOT_SET"

  /**
    * The receive block, useful if other actors want to manipulate the flow.
    * This is used by the UserParentActor to initiate the Websocket for a client.
    */
  override def receive: Receive = {
    case CreateAdapter(cId) =>
      clientId = cId
      log.info(s"Create Websocket for Client: $clientId")
      adapterActor ! SubscribeAdapter(clientId, wsActor())
      sender() ! websocketFlow
    case other =>
      log.info(s"Unexpected message from ${sender()}: $other")
  }

  /**
    * If this actor is killed directly, stop anything that we started running explicitly.
    * In our case unsubscribe the client in the AdapterActor
    */
  override def postStop(): Unit = {
    log.info(marker, s"Stopping $clientId: actor $self")
    adapterActor ! UnSubscribeAdapter(clientId)
  }

  /**
    * Generates a flow that can be used by the websocket.
    *
    * @return the flow of JSON
    */
  private lazy val websocketFlow: Flow[JsValue, JsValue, NotUsed] = {
    // Put the source and sink together to make a flow of hub source as output (aggregating all
    // AdapterMsgs as JSON to the browser) and the actor as the sink (receiving any JSON messages
    // from the browse), using a coupled sink and source.
    Flow.fromSinkAndSourceCoupled(jsonSink, hubSource)
      .watchTermination() { (_, termination) =>
        // When the flow shuts down, make sure this actor also stops.
        termination.foreach(_ => context.stop(self))
        NotUsed
      }
  }

  private val (hubSink, hubSource) = MergeHub.source[JsValue](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  private val jsonSink: Sink[JsValue, Future[Done]] = Sink.foreach { json =>
    // When the initiator runs the Adapter
    json.validate[AdapterMsg] match {
      case JsSuccess(runAdapter: RunJob, _) =>
        adapterActor ! runAdapter
      case JsSuccess(other, _) =>
        log.warning(marker, s"Unexpected message from ${sender()}: $other")
      case JsError(errors) =>
        log.error(marker, "Other than RunAdapter: " + errors.toString())
    }
  }

  /**
    * Creates an ActorRef that handles the outgoing AdapterMsg one by one and send them to the hub.
    */
  private def wsActor(): ActorRef = {
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
}

object UserActor {

  // used to inject the UserActors as childs of the UserParentActor
  trait Factory {
    def apply(id: String, adapterActor: ActorRef): Actor
  }

  case class CreateAdapter(clientId: String)
}




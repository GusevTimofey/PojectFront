package actors.network

import java.net.InetSocketAddress
import actors.network.NetworkMessages.{MatchedOrderNetworkMessage, NetworkMessageProto, OrderRequestNetworkMessage}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import actors.network.NetworkServer.InnerPing
import actors.networkCommunicators.LogicalActor.OrderDirection.Buy
import scala.concurrent.ExecutionContextExecutor

class NetworkServer extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val port1: Int = 9001
  val port2: Int = 9002

  val defaultSocketAddress: InetSocketAddress = new InetSocketAddress("0.0.0.0", port1)
  val peer = new InetSocketAddress("0.0.0.0", port2)

  IO(Tcp) ! Bind(self, defaultSocketAddress)

  system.scheduler.scheduleOnce(1.minutes, self, InnerPing)

  override def receive: Receive = {
    case InnerPing =>
      println("inner ping")
      IO(Tcp) ! Connect(peer)
    case Bound(localAddress) =>
      println(s"Local app $localAddress was successfully bound!")
    case CommandFailed(_: Bind) =>
      println(s"Exception during bounding!")
      context.stop(self)
    case Connected(remote, _) =>
      println(s"Got new connection from $remote")
      val handler: ActorRef = context.actorOf(PeerActor.props(remote, sender, self))
      sender ! Register(handler)
      sender ! ResumeReading
      handler ! OrderRequestNetworkMessage("ConnectedTo", "456", Buy, 11, 13)
    case message: NetworkMessageProto =>
      message match {
        case msg@OrderRequestNetworkMessage(assetId, clientId, direction, price, volume) =>
          println(s"${msg.toString}")
        case msg@MatchedOrderNetworkMessage(orderId, price, volume, matchedAddressId) =>
          println(s"${msg.toString}")
          sender ! MatchedOrderNetworkMessage("ooo", 1, 14, "qwertyuiopoiuyt")
      }
    case _ =>
  }

  def mainLogic: Receive = {
    case _ =>
  }
}

object NetworkServer {

  case object InnerPing

  def props: Props = Props(new NetworkServer)
}
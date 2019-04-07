package actors.network

import java.net.InetSocketAddress
import actors.network.NetworkMessages.{MatchedOrderNetworkMessage, NetworkMessageProto, OrderRequestNetworkMessage, TradeDirectiveNetworkMessage}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import actors.network.NetworkServer.{InnerPing, MessageFromPeer}
import actors.networkCommunicators.LogicalActor.OrderDirection.{Buy, Sell}
import scala.concurrent.ExecutionContextExecutor

class NetworkServer extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val port1: Int = 9003
  val port2: Int = 9111

  val defaultSocketAddress: InetSocketAddress = new InetSocketAddress("0.0.0.0", port1)
  val peer = new InetSocketAddress("0.0.0.0", port2)

  IO(Tcp) ! Bind(self, defaultSocketAddress)
  println(s"Starting client service on ${defaultSocketAddress.getAddress}...")

  system.scheduler.scheduleOnce(10.seconds, self, InnerPing)
  //system.scheduler.scheduleOnce(20.seconds, self, InnerPing)

  override def receive: Receive = {
    case InnerPing =>
      //println(s"inner ping to $peer")
      IO(Tcp) ! Connect(peer)
    case Bound(localAddress) =>
      println(s"Client service started successfully!")
    case CommandFailed(_: Bind) =>
      println(s"Exception during bounding!")
      context.stop(self)
    case Connected(remote, _) =>
      println(s"Got new connection to matcher with address: $remote.")
      val handler: ActorRef = context.actorOf(PeerActor.props(remote, sender, self))
      sender ! Register(handler)
      sender ! ResumeReading
      //handler ! OrderRequestNetworkMessage("wood", "stone", "Bob1", Sell, 90, 50) //node 1
      //handler ! OrderRequestNetworkMessage("wood", "stone", "Bob1", Sell, 10, 50) //node 2

      handler ! OrderRequestNetworkMessage("wood", "stone", "Alice", Buy, 120, 50) //node 3

    case MessageFromPeer(peer, message) =>
      message match {
        case msg@TradeDirectiveNetworkMessage(targetOrderOwnerClientId, counterPartyClientId, assetId, price, volume, order) =>
          println(s"Client got matched order: targetOrderOwnerClientId -> $targetOrderOwnerClientId, " +
            s"counterPartyClientId -> $counterPartyClientId, assetId -> $assetId, price -> $price, " +
            s"volume -> $volume, order -> $order")
        case msg@OrderRequestNetworkMessage(assetId, exchangeAssetId, clientId, direction, price, volume) =>
        case msg@MatchedOrderNetworkMessage(orderId, price, volume, matchedAddressId) =>
          println(s"${msg.toString} from $peer")
      }
    case _ =>
  }
}

object NetworkServer {

  case object InnerPing

  case class MessageFromPeer(peer: InetSocketAddress, message: NetworkMessageProto)

  def props: Props = Props(new NetworkServer)
}
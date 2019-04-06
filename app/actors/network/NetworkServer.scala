package actors.network

import java.net.InetSocketAddress

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import actors.network.NetworkMessages.{Ping, Pong}
import actors.network.NetworkServer.InnerPing

import scala.concurrent.ExecutionContextExecutor

class NetworkServer extends Actor {

  println("xui")
  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val port1: Int = 9001
  val port2: Int = 9002

  val defaultSocketAddress: InetSocketAddress = new InetSocketAddress("0.0.0.0", port2)
  val peer = new InetSocketAddress("0.0.0.0", port1)

  IO(Tcp) ! Bind(self, defaultSocketAddress)

  system.scheduler.schedule(1.seconds, 10.seconds, self, InnerPing)

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
      val handler: ActorRef = context.actorOf(PeerActor.props(remote, sender))
      sender ! Register(handler)
      sender ! ResumeReading
    case Ping => sender() ! Pong
    case Pong => println(s"Successfully bounded connection with $sender")
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
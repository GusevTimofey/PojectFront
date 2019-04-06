package actors.network

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import actors.network.NetworkMessages.{Ping, Pong}
import akka.io.Tcp
import akka.util.ByteString
import scala.util.Try

class PeerActor(remoteAddress: InetSocketAddress, listener: ActorRef) extends Actor {

  context.watch(listener)

  override def postStop(): Unit = {
    println(s"Peer handler $self to $remoteAddress is destroyed.")
    listener ! Close
  }

  override def receive: Receive = defaultLogic
    .orElse(readDataFromRemote)
    .orElse(writeDataToRemote)

  def defaultLogic: Receive = {
    case cc: ConnectionClosed =>
      println("Connection closed to : " + remoteAddress + ": " + cc.getErrorCause)
      context.stop(self)
    case fail@CommandFailed(cmd: Tcp.Command) =>
      println("Failed to execute command : " + cmd + s" cause ${fail.cause}")
      listener ! ResumeReading
    case message => println(s"Peer connection handler for $remoteAddress Got something strange: $message")
  }

  def readDataFromRemote: Receive = {
    case Received(data) =>
      Try(data.toArray.head match {
        case 1 =>
          println(s"Got $Ping from $remoteAddress on PeersHandler.")
          context.parent ! Ping
        case 2 =>
          println(s"Got $Pong from $remoteAddress on PeersHandler.")
          context.parent ! Pong
      })
      listener ! ResumeReading
  }

  def writeDataToRemote: Receive = {
    case Ping =>
      println(s"Got $Ping to $remoteAddress on PeersHandler.")
      listener ! Write(ByteString(Ping.typeId +: Ping.toProto.toByteArray))

    case Pong =>
      println(s"Got $Pong to $remoteAddress on PeersHandler.")
      listener ! Write(ByteString(Pong.typeId +: Pong.toProto.toByteArray))

    case _ =>
      println(s"Got something strange on PeerActor connected to $remoteAddress")
  }
}

object PeerActor {
  def props(remoteAddress: InetSocketAddress, listener: ActorRef): Props = Props(new PeerActor(remoteAddress, listener))
}
package actors.network

import NetworkMessageProto.{InnerMessagePing, InnerMessagePong}

object NetworkMessages {

  trait NetworkMessageType {

    val typeId: Byte
  }

  case object Ping extends NetworkMessageType {

    def toProto: InnerMessagePing = InnerMessagePing()

    def fromProto(innerPing: InnerMessagePing) = Ping

    override val typeId: Byte = 1: Byte
  }

  case object Pong extends NetworkMessageType {

    def toProto: InnerMessagePong = InnerMessagePong()

    def fromProto(innerPing: InnerMessagePong) = Pong

    override val typeId: Byte = 2: Byte

  }

}
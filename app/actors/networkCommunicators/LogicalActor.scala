package actors.networkCommunicators


class LogicalActor {

}

object LogicalActor {


  final case class Order(assetId: String,
                         clientId: String,
                         direction: OrderDirection,
                         price: Long,
                         volume: Long,
                         timestamp: Long,
                         status: OrderStatus = OrderStatus.Unknown)


  sealed trait OrderStatus

  object OrderStatus {

    case object Unknown extends OrderStatus

    case object Available extends OrderStatus

    case object NotAvailable extends OrderStatus

    case object Removed extends OrderStatus

    case object MakerOffline extends OrderStatus

  }

  sealed trait OrderDirection {
    val isSell: Boolean
    val isBuy: Boolean = !isSell
  }

  object OrderDirection {

    case object Sell extends OrderDirection {
      override val isSell: Boolean = true
    }

    case object Buy extends OrderDirection {
      override val isSell: Boolean = false
    }

  }
}
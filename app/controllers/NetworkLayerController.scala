package controllers

import java.net.InetSocketAddress

import actors.network.NetworkServer
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import javax.inject.Singleton
import play.api.mvc.{AbstractController, ControllerComponents}
import services.SimpleHttpRequests
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

@Singleton
class NetworkLayerController @Inject()(system: ActorSystem,
                                       httpC: SimpleHttpRequests,
                                       cc: ControllerComponents) extends AbstractController(cc) {

  implicit val ec: ExecutionContextExecutor = system.dispatcher


  val networkActor: ActorRef = system.actorOf(NetworkServer.props, "NetworkServer")
}

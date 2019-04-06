package controllers

import actors.network.NetworkServer
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import javax.inject.Singleton
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class NetworkLayerController @Inject()(system: ActorSystem,
                                       cc: ControllerComponents) extends AbstractController(cc) {

  println("xui123")
  val networkActor: ActorRef = system.actorOf(NetworkServer.props, "NetworkServer")
}

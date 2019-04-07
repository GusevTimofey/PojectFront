package services

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import com.google.inject.Inject
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.{ExecutionContext, Future}

class SimpleHttpRequests @Inject()(implicit val system: ActorSystem,
                                   implicit val materializer: Materializer,
                                   implicit val ec: ExecutionContext) {

  def simpleGet(peer: InetSocketAddress, address: String): Future[Seq[String]] =
    Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"/$address")
      .withEffectiveUri(securedConnection = false, Host(peer)))
      .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
      .map(_.utf8String)
      .map(decode[Seq[String]])
      .flatMap(_.fold(Future.failed, Future.successful))

  def simplePost(str: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = "/",
      entity = HttpEntity(ContentTypes.`application/json`, str.asJson.toString)
    ).withEffectiveUri(securedConnection = false, Host("")))

  def getState(peer: InetSocketAddress): Future[Seq[AssetState]] =
    Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = "/states")
      .withEffectiveUri(securedConnection = false, Host(peer)))
      .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
      .map(_.utf8String)
      .map(decode[Seq[AssetState]])
      .flatMap(_.fold(Future.failed, Future.successful))

  case class AssetState(assetName: String,
                        owner: String,
                        assetQty: Long)

}
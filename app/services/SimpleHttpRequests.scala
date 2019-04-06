package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import com.google.inject.Inject
import io.circe.parser.decode
import io.circe.syntax._
import scala.concurrent.{ExecutionContext, Future}

class SimpleHttpRequests @Inject()(implicit val system: ActorSystem,
                                   implicit val materializer: Materializer,
                                   implicit val ec: ExecutionContext) {

  def simpleGet(address: String): Future[Seq[String]] =
    Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = s"/"
    ).withEffectiveUri(securedConnection = false, Host("")))
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
}
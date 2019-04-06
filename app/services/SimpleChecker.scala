package services

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.util.control.NonFatal

class StrCheckAction(parser: BodyParsers.Default, str: String) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    if (str == "str") block(request).recoverWith {
        case NonFatal(_) => successful(Results.BadRequest)
      }(executionContext)
    else successful(Results.BadRequest)
}

class StrCheckActionFactory @Inject()(parser: BodyParsers.Default) {
  def apply(modifierId: String): StrCheckAction = new StrCheckAction(parser, modifierId)
}
package de.neuland.logging

import javax.inject.Inject
import akka.stream.Materializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.{Logger, Logging}
import play.api.mvc._

import scala.util.{Failure, Success}

class AccessLoggingFilter @Inject() (implicit val mat: Materializer) extends Filter with Logging {

  def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis()
    
    val headersString = request.headers.toMap
      .map { case (header, value) => s"$header:${value.mkString("\"", ",", "\"")}"}
      .mkString (" | ")
      
    val resultFuture = next(request)

    resultFuture.onComplete {
      case Failure(exception) =>
        val duration = System.currentTimeMillis() - startTime
        logger.info(s"request failed: method=${request.method} uri=${request.uri} headers=[$headersString] duration=${duration}ms error=" + "\"" + exception.getMessage + "\"")
      case Success(result) =>
        val duration = System.currentTimeMillis() - startTime
        logger.info(s"request succeeded: method=${request.method} uri=${request.uri} headers=[$headersString] status=${result.header.status} duration=${duration}ms")
    }

    resultFuture
  }

}

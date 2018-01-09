package de.neuland.logging

import javax.inject.Inject
import akka.stream.Materializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Logger
import play.api.mvc._

class AccessLoggingFilter @Inject() (implicit val mat: Materializer) extends Filter {

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis()
    
    val headersString = request.headers.toMap
      .map { case (header, value) => s"$header:${value.mkString("\"", ",", "\"")}"}
      .mkString (" | ")
      
    val resultFuture = next(request)

    resultFuture.onSuccess {
      case result =>
        val duration = System.currentTimeMillis() - startTime
        Logger.info(s"request succeeded: method=${request.method} uri=${request.uri} headers=[$headersString] status=${result.header.status} duration=${duration}ms")
    }
    
    resultFuture.onFailure {
      case t =>
        val duration = System.currentTimeMillis() - startTime
        Logger.info(s"request failed: method=${request.method} uri=${request.uri} headers=[$headersString] duration=${duration}ms error=" + "\"" + t.getMessage + "\"")
    }

    resultFuture
  }

}

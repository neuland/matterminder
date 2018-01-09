package de.neuland.logging

import javax.inject.Inject
import akka.stream.Materializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Logger
import play.api.mvc._

class AccessLoggingFilter @Inject() (implicit val mat: Materializer) extends Filter {


  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    Logger.debug(s"incoming request: method=${request.method} uri=${request.uri}")
    
    val startTime = System.currentTimeMillis()
    val resultFuture = next(request)

    resultFuture.foreach(result => {
      val duration = System.currentTimeMillis() - startTime
      Logger.info(s"method=${request.method} uri=${request.uri} status=${result.header.status} duration=${duration}ms")
    })

    resultFuture
  }
}

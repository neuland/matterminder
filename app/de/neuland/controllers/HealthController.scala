package de.neuland.controllers

import de.neuland.repositories.ConnectionCheckRepository
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class HealthController @Inject()(cc: ControllerComponents,
                                 connectionCheckRepository: ConnectionCheckRepository)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val logger = play.api.Logger(classOf[HealthController])

  def healthy: Action[AnyContent] = Action.async { _: Request[AnyContent] =>
    connectionCheckRepository.checkConnection()
      .map(_ => Ok)
      .recover {
        case throwable: Throwable =>
          logger.error("DB-Connection-Check fehlgeschlagen!", throwable)
          InternalServerError
      }
  }

}

package de.neuland.repositories

import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConnectionCheckRepository @Inject()()(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("database")

  def checkConnection(): Future[Unit] = {
    db.run(Query(1).result)
      .map(_ => ())
  }
}

package de.neuland.repositories

import de.neuland.parser.Schedule
import de.neuland.reminder.postgres.{Reminder, Reminders}
import slick.jdbc.PostgresProfile.api._

import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@Singleton
class ReminderRepository {

  private val reminders: TableQuery[Reminders] = TableQuery[Reminders]

  private val db = Database.forConfig("database")
  Await.ready(createTableIfItDoesNotExist(), Duration(1, TimeUnit.MINUTES))

  def getAll: Future[Seq[Reminder]] = {
    db.run(reminders.result)
  }
  
  def getById(id: String): Future[Option[Reminder]] = {
    db.run(reminders
      .filter(_.id === id)
      .take(1)
      .result
      .headOption)
  }
  
  def getByChannel(channel: String, webhookKey: String): Future[Seq[Reminder]] = {
    db.run(reminders
      .filter(reminder => reminder.recipient === channel.toLowerCase && reminder.webhookKey === webhookKey)
    .sortBy(_.id)
    .result)
  }

  def save(author: String, message: String, channelName: String, id: String, schedules: Seq[Schedule], webhookKey: String): Future[Int] = {
    val reminder = Reminder(id, author, channelName.toLowerCase, message, schedulesToSchedulesString(schedules), webhookKey)
    db.run(reminders += reminder)
  }

  def delete(id: String): Future[Int] = {
    db.run(reminders.filter(_.id === id).delete)
  }

  private def schedulesToSchedulesString(schedules: Seq[Schedule]) = {
    schedules.map(_.toString).mkString("%%%")
  }
  
  private def createTableIfItDoesNotExist(): Future[Unit] = {
    db.run(reminders.schema.createIfNotExists)
  }

}

package de.neuland.repositories

import de.neuland.parser.Schedule
import de.neuland.reminder.postgres.{Reminder, Reminders}

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery


class ReminderRepo {

  def getAll: List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL("jdbc:postgresql://localhost/matterminder", driver = "org.postgresql.Driver", user = "matterminder", password =  "matterminder") withSession {
      implicit session =>
        return reminders.list
    }
  }
  
  def getById(id: String): Option[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL("jdbc:postgresql://localhost/matterminder", driver = "org.postgresql.Driver", user = "matterminder", password =  "matterminder") withSession {
      implicit session =>
        return reminders.filter(_.id === id).firstOption
    }
  }
  
  def getByChannel(channel: String): List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL("jdbc:postgresql://localhost/matterminder", driver = "org.postgresql.Driver", user = "matterminder", password =  "matterminder") withSession {
      implicit session =>
        return reminders.filter(_.recipient === channel.toLowerCase).sortBy(_.id).list
    }
  }

  def save(author: String, message: String, channelName: String, id: String, schedules: Seq[Schedule]): Unit = {
    val reminder = Reminder(id, author, channelName.toLowerCase, message, schedulesToSchedulesString(schedules))
    
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL("jdbc:postgresql://localhost/matterminder", driver = "org.postgresql.Driver", user = "matterminder", password =  "matterminder") withSession {
      implicit session =>
        reminders += reminder
    }
  }

  def delete(id: String): Unit = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL("jdbc:postgresql://localhost/matterminder", driver = "org.postgresql.Driver", user = "matterminder", password =  "matterminder") withSession {
      implicit session =>
        reminders.filter(_.id === id).delete
    }
  }

  private def schedulesToSchedulesString(schedules: Seq[Schedule]) = {
    schedules.map(_.toString).mkString("%%%")
  }
}

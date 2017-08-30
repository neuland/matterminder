package de.neuland.repositories

import com.typesafe.config.{Config, ConfigFactory}
import de.neuland.parser.Schedule
import de.neuland.reminder.postgres.{Reminder, Reminders}

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery


class ReminderRepo {

  def getAll: List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$schema", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.list
    }
  }
  
  def getById(id: String): Option[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$schema", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.filter(_.id === id).firstOption
    }
  }
  
  def getByChannel(channel: String): List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$schema", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.filter(_.recipient === channel.toLowerCase).sortBy(_.id).list
    }
  }

  def save(author: String, message: String, channelName: String, id: String, schedules: Seq[Schedule]): Unit = {
    val reminder = Reminder(id, author, channelName.toLowerCase, message, schedulesToSchedulesString(schedules))
    
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$schema", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        reminders += reminder
    }
  }

  def delete(id: String): Unit = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$schema", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        reminders.filter(_.id === id).delete
    }
  }

  private def schedulesToSchedulesString(schedules: Seq[Schedule]) = {
    schedules.map(_.toString).mkString("%%%")
  }

  private val config: Config = ConfigFactory.load("postgres.conf")
  val server: String = config.getString("server")
  val schema: String = config.getString("schema")
  val dbUser: String = config.getString("dbuser")
  val dbPassword: String = config.getString("dbpassword")
}

package de.neuland.repositories

import com.typesafe.config.{Config, ConfigFactory}
import de.neuland.parser.Schedule
import de.neuland.reminder.postgres.{Reminder, Reminders}
import slick.jdbc.meta.MTable
import play.api.Logger

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery


class ReminderRepo {

  private val config: Config = ConfigFactory.load("postgres.conf")
  private val server: String = "jdbc:postgresql://" + config.getString("server")
  private val database: String = config.getString("database")
  private val dbUser: String = config.getString("dbuser")
  private val dbPassword: String = config.getString("dbpassword")
  
  Logger.info(s"using db credentials: user=${dbUser}, pw=${dbPassword}, db=${database}")

  createTableIfItDoesNotExist()

  def getAll: List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.list
    }
  }
  
  def getById(id: String): Option[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.filter(_.id === id).firstOption
    }
  }
  
  def getByChannel(channel: String): List[Reminder] = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        return reminders.filter(_.recipient === channel.toLowerCase).sortBy(_.id).list
    }
  }

  def save(author: String, message: String, channelName: String, id: String, schedules: Seq[Schedule]): Unit = {
    val reminder = Reminder(id, author, channelName.toLowerCase, message, schedulesToSchedulesString(schedules))
    
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        reminders += reminder
    }
  }

  def delete(id: String): Unit = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
      implicit session =>
        reminders.filter(_.id === id).delete
    }
  }

  private def schedulesToSchedulesString(schedules: Seq[Schedule]) = {
    schedules.map(_.toString).mkString("%%%")
  }
  
  private def createTableIfItDoesNotExist(): Unit = {
    val reminders: TableQuery[Reminders] = TableQuery[Reminders]
    Database.forURL(s"$server/$database", driver = "org.postgresql.Driver", user = dbUser, password =  dbPassword) withSession {
     implicit session =>
       val tableAlreadyExists: Boolean = MTable.getTables(reminders.baseTableRow.tableName).firstOption.nonEmpty
       if(!tableAlreadyExists) {
         reminders.ddl.create
       }
    }
  }

}

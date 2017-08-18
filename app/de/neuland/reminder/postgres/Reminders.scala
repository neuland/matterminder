package de.neuland.reminder.postgres

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.{ProvenShape, Tag}

class Reminders(tag: Tag) extends Table[Reminder](tag, "reminders") {
  def id: Column[String] = column[String]("id", O.PrimaryKey, O.NotNull)
  def author: Column[String] = column[String]("author", O.NotNull)
  def recipient: Column[String] = column[String]("recipient", O.NotNull)
  def message: Column[String] = column[String]("message", O.NotNull)
  def schedules: Column[String] = column[String]("schedules", O.NotNull)
  def * : ProvenShape[Reminder] = (id, author, recipient, message, schedules) <> (Reminder.tupled, Reminder.unapply)
}

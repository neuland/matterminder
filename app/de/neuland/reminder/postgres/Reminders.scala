package de.neuland.reminder.postgres

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.{Index, ProvenShape, Tag}

class Reminders(tag: Tag) extends Table[Reminder](tag, "reminders") {
  def id: Column[String] = column[String]("id", O.PrimaryKey, O.NotNull)
  def author: Column[String] = column[String]("author", O.NotNull)
  def recipient: Column[String] = column[String]("recipient", O.NotNull)
  def message: Column[String] = column[String]("message", O.NotNull)
  def schedules: Column[String] = column[String]("schedules", O.NotNull)
  def webhookKey: Column[String] = column[String]("webhookkey", O.NotNull)
  def * : ProvenShape[Reminder] = (id, author, recipient, message, schedules, webhookKey) <> (Reminder.tupled, Reminder.unapply)
  def idx_author: Index = index("idx_author", author)
  def idx_recipient: Index = index("idx_recipient", recipient)
}

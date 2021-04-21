package de.neuland.reminder.postgres

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class Reminders(tag: Tag) extends Table[Reminder](tag, "reminders") {
  def id = column[String]("id", O.PrimaryKey)
  def author = column[String]("author")
  def recipient = column[String]("recipient")
  def message = column[String]("message")
  def schedules = column[String]("schedules")
  def webhookKey = column[String]("webhookkey")
  def * = (id, author, recipient, message, schedules, webhookKey) <> (Reminder.tupled, Reminder.unapply)
  def idx_author = index("idx_author", author)
  def idx_recipient = index("idx_recipient", recipient)
}

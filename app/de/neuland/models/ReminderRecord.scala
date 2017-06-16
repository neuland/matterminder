package de.neuland.models

import org.mongodb.scala.bson.ObjectId

object ReminderRecord {
  def apply(author: String, recipient: String, note: String): ReminderRecord =
    ReminderRecord(new ObjectId(), author, recipient, note)
}

case class ReminderRecord(_id: ObjectId, author: String, recipient: String, message: String)
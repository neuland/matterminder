package de.neuland.models

import org.mongodb.scala.bson.ObjectId

object Reminder {
  def apply(author: String, recipient: String, note: String): Reminder =
    Reminder(new ObjectId(), author, recipient, note)
}

case class Reminder(_id: ObjectId, author: String, recipient: String, message: String)
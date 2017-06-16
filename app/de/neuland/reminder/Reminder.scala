package de.neuland.reminder

import akka.actor.Actor
import de.neuland.reminder.Reminder.Remind
import play.api.Logger

object Reminder {
  case class Remind()
}

class Reminder(message: String, id: String) extends Actor {
  
  override def receive: Receive = {
    case Remind => {
      Logger.info("Don't forget to " + message)
    }
    Logger.info("received message")
  }
  
}

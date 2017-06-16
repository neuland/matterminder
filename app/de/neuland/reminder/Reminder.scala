package de.neuland.reminder

import akka.actor.Actor
import de.neuland.reminder.Reminder.Remind

object Reminder {
  case object Remind
}

class Reminder(message: String, id: String) extends Actor {
  
  override def receive: Receive = {
    case Remind => {
     println("Don't forget to " + message)
    }
  }
  
}

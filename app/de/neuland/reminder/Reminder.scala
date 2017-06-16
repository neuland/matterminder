package de.neuland.reminder

import akka.actor.{Actor, ActorRef}
import de.neuland.client.WebhookClient.SendRemind
import de.neuland.reminder.Reminder.Remind

object Reminder {
  case object Remind
}

class Reminder(message: String, channel: String, id: String, webhookClient: ActorRef) extends Actor {
  
  override def receive: Receive = {
    case Remind => {
     webhookClient ! SendRemind(message, channel)
    }
  }
  
}

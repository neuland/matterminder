package de.neuland.reminder

import akka.actor.{Actor, ActorRef}
import de.neuland.client.WebhookClient.SendRemind
import de.neuland.parser.Schedule
import de.neuland.reminder.ReminderActor.Remind

object ReminderActor {
  case object Remind
}

class ReminderActor(val message: String, val channel: String, val id: String, val schedules: Seq[Schedule], webhookClient: ActorRef) extends Actor {
  
  override def receive: Receive = {
    case Remind =>
      webhookClient ! SendRemind(message, channel)
  }
  
}

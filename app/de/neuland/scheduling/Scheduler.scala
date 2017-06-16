package de.neuland.scheduling

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime}
import javax.inject.{Inject, Named, Singleton}

import akka.actor._
import de.neuland.reminder.Reminder.Remind
import de.neuland.scheduling.Scheduler.ScheduleReminder
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Scheduler {
  def props = Props[Scheduler]

  case class ScheduleReminder(reminderId: String, when: LocalDateTime)
}


class Scheduler @Inject() (system: ActorSystem) extends Actor {
  
  private var reminders = scala.collection.mutable.Map[String, List[String]]()
  private val dateTimeToKeyFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")


  system.scheduler.schedule(secondsUntilFullMinute(LocalDateTime.now()).seconds, 1.minutes) {
    executeTasks()
  }
  
  override def receive: Receive = {
    case ScheduleReminder(reminder: String, when: LocalDateTime) =>
      if(when.isAfter(LocalDateTime.now())) {
        val key = toKey(when)
        val presentReminders: List[String] = reminders.getOrElse(key, List[String]())
        reminders(key) = reminder :: presentReminders
      }
  }

  def executeTasks(): Unit = {
    val key = toKey(LocalDateTime.now())
    val remindersToExecute = reminders.getOrElse(key, List[String]())
    Logger.info("all reminders: " + reminders)
    reminders -= key
    remindersToExecute.foreach(reminderId => system.actorSelection(reminderId).resolveOne(10.seconds).onComplete {
      case Success(actorRef) => actorRef ! Remind()
      case Failure(ex) => Logger.error("Could not find Reminder with id '" + reminderId + "'")
    })
    remindersToExecute.foreach(reminder => Logger.info("remind message sent!"))
  }

  private def toKey(dateTime: LocalDateTime) = {
    dateTime.format(dateTimeToKeyFormatter)
  }

  private def secondsUntilFullMinute(localDateTime: LocalDateTime) = 60 - LocalDateTime.now().getSecond()
}

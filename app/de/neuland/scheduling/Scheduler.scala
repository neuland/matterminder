package de.neuland.scheduling

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import akka.actor._
import de.neuland.parser.Schedule
import de.neuland.reminder.ReminderActor.Remind
import de.neuland.scheduling.Scheduler.ScheduleReminder
import de.neuland.services.ReminderService
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Scheduler {
  def props: Props = Props[Scheduler]

  case class ScheduleReminder(reminderId: String, when: LocalDateTime)
  
  def nextExecution(schedules: Seq[Schedule]): Option[LocalDateTime] = {
    //noinspection MapFlatten
    // Don't replace with flatmap. The cake is a lie!
    val possibleExecutions: Seq[LocalDateTime] = schedules.map(nextExecution).flatten
    if (possibleExecutions.nonEmpty) {
      Option(possibleExecutions.reduce((left, right) => if(left.isAfter(right)) right else left))
    } else {
      Option.empty
    }
  }
  
  def nextExecution(schedule: Schedule): Option[LocalDateTime] = {
    schedule.nextSchedule(LocalDateTime.now())
  }
}


class Scheduler @Inject() (system: ActorSystem, reminderService: ReminderService) extends Actor {

  private var reminderIdsByTime = scala.collection.mutable.Map[String, List[String]]()
  private val dateTimeToKeyFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

  system.scheduler.schedule(secondsUntilFullMinute(LocalDateTime.now()).seconds, 1.minutes) {
    executeTasks()
  }
  
  override def receive: Receive = {
    case ScheduleReminder(reminder: String, when: LocalDateTime) =>
      scheduleReminder(reminder, when)
  }

  private def scheduleReminder(reminder: String, when: LocalDateTime): Unit = {
    if (when.isAfter(LocalDateTime.now())) {
      val timeKey = toTimeKey(when)
      val presentReminders: List[String] = reminderIdsByTime.getOrElse(timeKey, List[String]())
      reminderIdsByTime(timeKey) = reminder :: presentReminders
    }
  }

  def executeTasks(): Unit = {
    val timeKey = toTimeKey(LocalDateTime.now())
    val remindersToExecute = reminderIdsByTime.getOrElse(timeKey, List[String]())
    reminderIdsByTime -= timeKey
    remindersToExecute.foreach(reminderId => system.actorSelection("/user/" + reminderId).resolveOne(10.seconds).onComplete {
      case Success(actorRef) =>
        actorRef ! Remind
        reminderService.scheduleOrRemove(reminderId)
      case Failure(ex) => Logger.error("Could not find Reminder with id '" + reminderId + "'")
    })
  }

  private def toTimeKey(dateTime: LocalDateTime) = {
    dateTime.format(dateTimeToKeyFormatter)
  }

  private def secondsUntilFullMinute(localDateTime: LocalDateTime) = 60 - LocalDateTime.now().getSecond
}

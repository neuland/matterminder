package de.neuland.scheduling

import akka.actor._
import de.neuland.parser.Schedule
import de.neuland.reminder.ReminderActor.Remind
import de.neuland.scheduling.Scheduler.{ScheduleReminder, UnscheduleReminder}
import de.neuland.services.ReminderService
import play.api.Logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Scheduler {
  def props: Props = Props[Scheduler]()

  case class ScheduleReminder(reminderId: String, when: LocalDateTime)
  case class UnscheduleReminder(reminderId: String)
  
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


class Scheduler @Inject() (system: ActorSystem, reminderService: ReminderService) extends Actor with Logging {

  private var reminderIdsByTime = scala.collection.mutable.Map[String, List[String]]()
  private val dateTimeToKeyFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

  system.scheduler.scheduleAtFixedRate(secondsUntilFullMinute(LocalDateTime.now()).seconds, 1.minutes) {
    () => executeTasks()
  }
  
  override def receive: Receive = {
    case ScheduleReminder(reminder: String, when: LocalDateTime) =>
      scheduleReminder(reminder, when)
    case UnscheduleReminder(reminder: String) =>
      unscheduleReminder(reminder)
  }

  private def scheduleReminder(reminder: String, when: LocalDateTime): Unit = {
    if (when.isAfter(LocalDateTime.now())) {
      val timeKey = toTimeKey(when)
      val presentReminders: List[String] = reminderIdsByTime.getOrElse(timeKey, List[String]())
      reminderIdsByTime(timeKey) = reminder :: presentReminders
    }
  }
  
  private def unscheduleReminder(reminderId: String): Unit = {
    reminderIdsByTime
      .withFilter(entry => entry._2.contains(reminderId))
      .foreach(entry => {
        val remindersWithoutRemovedOne = entry._2.filter(reminder => reminder != reminderId)
        if(remindersWithoutRemovedOne.isEmpty) {
          reminderIdsByTime -= entry._1
        } else {
          reminderIdsByTime(entry._1) = remindersWithoutRemovedOne
        }
      })
  }

  private def executeTasks(): Unit = {
    val timeKey = toTimeKey(LocalDateTime.now())
    val remindersToExecute = reminderIdsByTime.getOrElse(timeKey, List[String]())
    reminderIdsByTime -= timeKey
    remindersToExecute.foreach(reminderId => system.actorSelection("/user/" + reminderId).resolveOne(10.seconds).onComplete {
      case Success(actorRef) =>
        actorRef ! Remind
        reminderService.scheduleOrRemove(reminderId)
      case Failure(ex) => logger.error("Could not find Reminder with id '" + reminderId + "'", ex)
    })
  }

  private def toTimeKey(dateTime: LocalDateTime) = {
    dateTime.format(dateTimeToKeyFormatter)
  }

  private def secondsUntilFullMinute(localDateTime: LocalDateTime) = 60 - LocalDateTime.now().getSecond
}

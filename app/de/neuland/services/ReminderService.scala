package de.neuland.services

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import de.neuland.command.SlashCommand
import de.neuland.parser._
import de.neuland.reminder.ReminderActor
import de.neuland.reminder.postgres.Reminder
import de.neuland.repositories.ReminderRepo
import de.neuland.scheduling.Scheduler
import de.neuland.scheduling.Scheduler.{ScheduleReminder, UnscheduleReminder}
import fastparse.core.Parsed
import play.api.Logger

@Singleton
class ReminderService @Inject() (@Named("scheduler")scheduler: ActorRef, system: ActorSystem, @Named("webhookClient") webhookClient: ActorRef, reminderRepository: ReminderRepo) {

  private val parser = new Parser()

  def createReminder(slashCommand: SlashCommand): Unit = {
    val parsedCommand: Parsed[ParseResult, Char, String] = parser.reminder.parse(slashCommand.text)
    parsedCommand match {
      case Parsed.Success(ParseResult(channel, message, schedules), _) =>
        Logger.debug(s"channel: $channel / message: $message / schedules: $schedules")
        
        val channelName = getChannelName(channel, slashCommand.userName, slashCommand.channelName)
        
        val id = UUID.randomUUID().toString
        startReminderActor(id, message, channelName, schedules)
        reminderRepository.save(slashCommand.userName, message, channelName, id, schedules)
      case other =>
        Logger.warn("failed parsing /remind command! " + other)
    }
  }

  def scheduleOrRemove(reminderId: String): Unit = {
    val maybeReminder = reminderRepository.getById(reminderId)
    if(maybeReminder.nonEmpty) {
      val maybeNextExecution: Option[LocalDateTime] = maybeReminder.map(_.schedules).map(toSchedules).flatMap(Scheduler.nextExecution)
      if(maybeNextExecution.nonEmpty) {
        scheduler ! ScheduleReminder(reminderId, maybeNextExecution.get)
      } else {
        reminderRepository.delete(reminderId)
      }
    }
  }
  
  def getRemindersForChannel(channel: String): List[String] = {
    reminderRepository.getByChannel(channel).map(reminder => s"* **id: '${reminder.id}'** / author: '${reminder.author}' / message: '${reminder.message}'")
  }
  
  def doesReminderExist(remindeId: String): Boolean = {
    reminderRepository.getById(remindeId).nonEmpty
  }
  
  def delete(reminderId: String): Unit = {
    scheduler ! UnscheduleReminder(reminderId)
    reminderRepository.delete(reminderId)
  }

  private def scheduleExistingReminders(): Unit = {
    val reminders = reminderRepository.getAll
    println(s"Loaded reminders: ${reminders.map(_.id).mkString(", ")}")
    reminders.foreach(startReminderActor)
  }

  private def startReminderActor(reminder: Reminder): Unit = {
    val schedulesString = reminder.schedules
    val schedules = toSchedules(schedulesString)
    if(schedules.nonEmpty) {
      startReminderActor(reminder.id, reminder.message, reminder.recipient, schedules)
    } else {
      Logger.warn(s"Could start reminder actor for reminder '${reminder.id}' since its schedules could not be parsed.")
    }
  }

  private def startReminderActor(reminderId: String, message: String, channelName: String, schedules: Seq[Schedule]): Unit = {
    val maybeNextExecution = Scheduler.nextExecution(schedules)
    if (maybeNextExecution.nonEmpty) {
      system.actorOf(Props(new ReminderActor(message, channelName, reminderId, schedules, webhookClient)), name = reminderId)
      scheduler ! ScheduleReminder(reminderId, maybeNextExecution.get)
    }
  }

  private def getChannelName(target: Target, originUserName: String, originChannelName: String): String = {
    /*
      me and @[ownUserName] do not work if [me] is the creator of the webhook
      see:
       - https://forum.mattermost.org/t/solved-incoming-webhook-cannot-send-result-to-user/2306
       - https://mattermost.uservoice.com/forums/306457-general/suggestions/15697014-add-option-to-direct-message-yourself
     */
    
    
    target match {
      case User(name) => s"@$name"
      case Channel(name) => s"$name"
      case Me => s"@$originUserName"
      case _ =>  s"@$originChannelName"
    }
    
  }

  def toSchedules(schedulesString: String): Seq[Schedule] = {
    schedulesString.split("%%%").flatMap(Schedule.fromString)
  }
  
  scheduleExistingReminders()

}

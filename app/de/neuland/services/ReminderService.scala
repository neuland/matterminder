package de.neuland.services

import akka.actor.{ActorRef, ActorSystem, Props}
import de.neuland.command.SlashCommand
import de.neuland.parser._
import de.neuland.reminder.ReminderActor
import de.neuland.reminder.postgres.Reminder
import de.neuland.repositories.ReminderRepository
import de.neuland.scheduling.Scheduler
import de.neuland.scheduling.Scheduler.{ScheduleReminder, UnscheduleReminder}
import fastparse.Parsed
import play.api.Logging

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Named, Singleton}
import scala.collection.immutable.ArraySeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReminderService @Inject() (@Named("scheduler")scheduler: ActorRef,
                                 system: ActorSystem,
                                 @Named("webhookClient") webhookClient: ActorRef,
                                 reminderRepository: ReminderRepository,
                                 webhookAuthenticationService: WebhookAuthenticationService) extends Logging {

  private val parser = new Parser()

  def createReminder(slashCommand: SlashCommand): Future[Boolean] = {
    parser.parseReminder(slashCommand.text.trim) match {
      case Parsed.Success(ParseResult(channel, message, schedules), _) =>
        logger.debug(s"channel: $channel / message: $message / schedules: $schedules")
        
        val channelName = getChannelName(channel, slashCommand.userName, slashCommand.channelName)
        
        val id = UUID.randomUUID().toString
        val webhookKey = webhookAuthenticationService.getWebhookKeyForCommandToken(slashCommand.token)
        startReminderActor(id, message, channelName, schedules, webhookKey)
        reminderRepository
          .save(slashCommand.userName, message, channelName, id, schedules, webhookKey)
          .map(_ > 0)
      case other =>
        logger.warn("failed parsing /remind command! " + other)
        Future.successful(false)
    }
  }

  def scheduleOrRemove(reminderId: String): Unit = {
    reminderRepository
      .getById(reminderId)
      .map { maybeReminder =>
        if(maybeReminder.nonEmpty) {
          val maybeNextExecution: Option[LocalDateTime] = maybeReminder.map(_.schedules).map(toSchedules).flatMap(Scheduler.nextExecution)
          if(maybeNextExecution.nonEmpty) {
            scheduler ! ScheduleReminder(reminderId, maybeNextExecution.get)
          } else {
            reminderRepository.delete(reminderId)
          }
        }
      }
  }
  
  def getRemindersForChannel(channel: String, slashCommandToken: String): Future[Seq[String]] = {
    val webhookKey = webhookAuthenticationService.getWebhookKeyForCommandToken(slashCommandToken)
    reminderRepository
      .getByChannel(channel, webhookKey)
      .map(_.map(reminder => s"* **id: '${reminder.id}'** / author: '${reminder.author}' / message: '${reminder.message}'"))
  }
  
  def doesReminderExist(reminderId: String): Future[Boolean] = {
    reminderRepository
      .getById(reminderId)
      .map(_.nonEmpty)
  }
  
  def delete(reminderId: String): Future[Boolean] = {
    scheduler ! UnscheduleReminder(reminderId)
    reminderRepository
      .delete(reminderId)
      .map(_ > 0)
  }

  private def scheduleExistingReminders(): Unit = {
    reminderRepository.getAll.map { reminders =>
      println(s"Loaded reminders: ${reminders.map(_.id).mkString(", ")}")
      reminders.foreach(startReminderActor)
    }
  }

  private def startReminderActor(reminder: Reminder): Unit = {
    val schedulesString = reminder.schedules
    val schedules = toSchedules(schedulesString)
    if(schedules.nonEmpty) {
      startReminderActor(reminder.id, reminder.message, reminder.recipient, schedules, reminder.webhookKey)
    } else {
      logger.warn(s"Could start reminder actor for reminder '${reminder.id}' since its schedules could not be parsed.")
    }
  }

  private def startReminderActor(reminderId: String, message: String, channelName: String, schedules: Seq[Schedule], webhookKey: String): Unit = {
    val maybeNextExecution = Scheduler.nextExecution(schedules)
    if (maybeNextExecution.nonEmpty) {
      system.actorOf(Props(new ReminderActor(message, channelName, reminderId, schedules, webhookClient, webhookKey)), name = reminderId)
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
    ArraySeq.unsafeWrapArray(schedulesString.split("%%%")).flatMap(Schedule.fromString)
  }
  
  scheduleExistingReminders()

}

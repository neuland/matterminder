package de.neuland.services

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem, Props}
import de.neuland.models.SlashCommand
import de.neuland.parser._
import de.neuland.reminder.Reminder
import de.neuland.repositories.ReminderRepository
import de.neuland.scheduling.Scheduler.ScheduleReminder
import fastparse.core.Parsed
import play.api.Logger

class ReminderService @Inject() (@Named("scheduler") scheduler: ActorRef, system: ActorSystem, @Named("webhookClient") webhookClient: ActorRef, reminderRepository: ReminderRepository) {
  
  private val parser = new Parser()

  def createReminder(slashCommand: SlashCommand): Unit = {
    //reminderRepository.save(ReminderRecord(
    //  slashCommand.userName,
    //  slashCommand.channelName,
    //  slashCommand.text
    //))

    val parsedCommand: Parsed[ParseResult, Char, String] = parser.reminder.parse(slashCommand.text)
    parsedCommand match {
      case Parsed.Success(ParseResult(channel, message, schedules), _) =>
        Logger.debug(s"channel: $channel / message: $message / schedules: $schedules")
        
        val channelName = getChannelName(channel, slashCommand.userName, slashCommand.channelName)
        
        val id = UUID.randomUUID().toString
        val reminder = system.actorOf(Props(new Reminder(message, channelName, id, webhookClient)), name = id)
        scheduler ! ScheduleReminder(id, LocalDateTime.now().plusMinutes(1))
      case other =>
        Logger.warn("failed parsing /remind command! " + other)
    }
    

  }

  def scheduleExistingReminders(): Unit = {
    // load all reminders from repository
    // notify scheduling actor
  }

  def getChannelName(target: Target, originUserName: String, originChannelName: String): String = {
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

}

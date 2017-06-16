package de.neuland.services

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem, Props}
import de.neuland.client.WebhookClient
import de.neuland.models.{ReminderRecord, SlashCommand}
import de.neuland.reminder.Reminder
import de.neuland.repositories.ReminderRepository
import de.neuland.scheduling.Scheduler.ScheduleReminder

class ReminderService @Inject() (@Named("scheduler") scheduler: ActorRef, system: ActorSystem, @Named("webhookClient") webhookClient: ActorRef, reminderRepository: ReminderRepository) {

  def createReminder(slashCommand: SlashCommand): Unit = {
    reminderRepository.save(ReminderRecord(
      slashCommand.userName,
      slashCommand.channelName,
      slashCommand.text
    ))

    val id = UUID.randomUUID().toString
    val reminder = system.actorOf(Props(new Reminder(slashCommand.text, slashCommand.channelName, id, webhookClient)), name = id)
    scheduler ! ScheduleReminder(id, LocalDateTime.now().plusMinutes(1))
  }

  def scheduleExistingReminders(): Unit = {
    // load all reminders from repository
    // notify scheduling actor
  }

}

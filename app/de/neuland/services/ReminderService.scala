package de.neuland.services

import javax.inject.Inject

import de.neuland.models.SlashCommand
import de.neuland.repositories.ReminderRepository

class ReminderService @Inject() (reminderRepository: ReminderRepository) {

  def createReminder(slashCommand: SlashCommand): Unit = {
    // save to repository
    // notify scheduling actor
  }

  def scheduleExistingReminders(): Unit = {
    // load all reminders from repository
    // notify scheduling actor
  }

}

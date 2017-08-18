package de.neuland.controllers

import javax.inject.{Inject, Singleton}

import de.neuland.models.SlashCommand
import de.neuland.services.ReminderService
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}

@Singleton
class CommandController @Inject() (reminderService: ReminderService) extends Controller {
  
  def executeCommand = Action { request =>
    request.body.asFormUrlEncoded.map(SlashCommand(_)) match {
      case Some(slashCommand) =>
        
        slashCommand.commandType match {
          case "create" =>
            createReminder(slashCommand)
          case "list" =>
            listReminders(slashCommand)
          case "delete" =>
            deleteReminder(slashCommand)
          case unknownCommand => BadRequest(s"unknown command: '$unknownCommand'")
            
        }
      case None =>
        BadRequest("could not parse request")
    }
  }

  private def createReminder(slashCommand: SlashCommand) = {
    reminderService.createReminder(slashCommand)
    answer("reminder saved: " + slashCommand.text)
  }

  private def listReminders(slashCommand: SlashCommand) = {
    val channel = if (slashCommand.command.trim.nonEmpty) {
      slashCommand.command
    } else {
      slashCommand.channelName
    }
    val reminders = reminderService.getRemindersForChannel(channel)
    val answerText = if (reminders.isEmpty) {
      s"no reminders in channel '$channel'"
    } else {
      s"reminders in channel '$channel' (${reminders.length}): \n" + reminders.mkString("\n")
    }
    answer(answerText)
  }

  private def deleteReminder(slashCommand: SlashCommand): Result = {
    val reminderId = slashCommand.command.trim
    if (reminderId.isEmpty) {
      answer("**No reminder id given!**")
    } else if(!reminderService.doesReminderExist(reminderId)) {
      answer(s"**Reminder '$reminderId' does not exist!**")
    } else {
      reminderService.delete(reminderId)
      answer(s"Reminder '$reminderId' was successfully deleted.")
    }
  }
  
  private def answer(message: String): Result = {
    Ok(Json.obj(
      "response_type" -> "in_channel",
      "text" -> message,
      "username" -> "Matterminder"
    ))
  }
}
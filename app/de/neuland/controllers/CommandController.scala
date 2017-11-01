package de.neuland.controllers

import javax.inject.{Inject, Singleton}

import de.neuland.command.{Commands, SlashCommand}
import de.neuland.services.ReminderService
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}

@Singleton
class CommandController @Inject() (reminderService: ReminderService) extends Controller {

  def executeCommand = Action { request =>
    request.body.asFormUrlEncoded.map(SlashCommand(_)) match {
      case Some(slashCommand) =>
        
        slashCommand.commandType match {
          case "help" =>
            showHelp()
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

  private def showHelp(): Result = {
    answer("**available commads:**\n" + Commands.commandHelpTexts.mkString("\n***\n"))
  }

  private def createReminder(slashCommand: SlashCommand) = {
    val successfullyCreated = reminderService.createReminder(slashCommand)
    if (successfullyCreated) {
      answer(s":white_check_mark: reminder saved: ${slashCommand.text}")
    } else {
      answer(s":x: could not save reminder: ${slashCommand.text}")
    }
  }

  private def listReminders(slashCommand: SlashCommand) = {
    val channel = if (slashCommand.command.trim.nonEmpty) {
      slashCommand.command
    } else {
      slashCommand.channelName
    }
    val reminders = reminderService.getRemindersForChannel(channel, slashCommand.token)
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
      answer(":x: **No reminder id given!**")
    } else if(!reminderService.doesReminderExist(reminderId)) {
      answer(s":x: **Reminder '$reminderId' does not exist!**")
    } else {
      reminderService.delete(reminderId)
      answer(s":white_check_mark: Reminder '$reminderId' was successfully deleted.")
    }
  }
  
  private def answer(message: String): Result = {
    Ok(Json.obj(
      "response_type" -> "in_channel",
      "text" -> message,
      "username" -> "Matterminder",
      "response_type" -> "ephemeral"
    ))
  }
}
package de.neuland.controllers

import de.neuland.command.{Commands, SlashCommand}
import de.neuland.services.ReminderService
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommandController @Inject() (cc: ControllerComponents,
                                   reminderService: ReminderService)
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def executeCommand: Action[AnyContent] = Action.async { request =>
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
          case unknownCommand =>
            Future.successful(BadRequest(s"unknown command: '$unknownCommand'"))
            
        }
      case None =>
        Future.successful(BadRequest("could not parse request"))
    }
  }

  private def showHelp(): Future[Result] = {
    Future.successful(answer("**available commads:**\n" + Commands.commandHelpTexts.mkString("\n***\n")))
  }

  private def createReminder(slashCommand: SlashCommand) = {
    reminderService
      .createReminder(slashCommand)
      .map { successfullyCreated =>
        if (successfullyCreated) {
          answer(s":white_check_mark: reminder saved: ${slashCommand.text}")
        } else {
          answer(s":x: could not save reminder: ${slashCommand.text}")
        }
      }
  }

  private def listReminders(slashCommand: SlashCommand) = {
    val channel = if (slashCommand.command.trim.nonEmpty) {
      slashCommand.command
    } else {
      slashCommand.channelName
    }
    reminderService
      .getRemindersForChannel(channel, slashCommand.token)
      .map{ reminders =>
        val answerText = if (reminders.isEmpty) {
          s"no reminders in channel '$channel'"
        } else {
          s"reminders in channel '$channel' (${reminders.length}): \n" + reminders.mkString("\n")
        }
        answer(answerText)
      }
  }

  private def deleteReminder(slashCommand: SlashCommand): Future[Result] = {
    val reminderId = slashCommand.command.trim
    if (reminderId.isEmpty) {
      Future.successful(answer(":x: **No reminder id given!**"))
    } else {
      reminderService
        .doesReminderExist(reminderId)
        .flatMap { reminderExists =>
          if(reminderExists) {
            reminderService
              .delete(reminderId)
              .map(_ => answer(s":white_check_mark: Reminder '$reminderId' was successfully deleted."))
              .recover {
                case t: Throwable => answer(s" :heavy_exclamation_mark: Reminder '$reminderId' could not be deleted: ${t.getMessage}")
              }
          } else {
            Future.successful(answer(s":x: **Reminder '$reminderId' does not exist!**"))
          }
        }
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
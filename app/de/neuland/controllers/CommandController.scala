package de.neuland.controllers

import javax.inject.{Inject, Singleton}

import de.neuland.models.SlashCommand
import de.neuland.services.ReminderService
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

@Singleton
class CommandController @Inject() (reminderService: ReminderService) extends Controller {
  
  def executeCommand = Action { request =>
    Logger.info("got request")
    request.body.asFormUrlEncoded.map(SlashCommand(_)) match {
      case Some(slashCommand) =>
        
        slashCommand.command match {
          case "/remind" =>
            reminderService.createReminder(slashCommand)
    
            Ok(Json.obj(
              "response_type" -> "in_channel",
              "text" -> ( "reminder saved: " + slashCommand.text),
              "username" -> "Matterminder"
            ))
        }
      case None =>
        BadRequest("")
    }
  }

}
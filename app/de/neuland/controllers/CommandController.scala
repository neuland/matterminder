package de.neuland.controllers

import javax.inject.{Inject, Singleton}

import de.neuland.models.SlashCommand
import de.neuland.services.ReminderService
import play.api.mvc.{Action, Controller}

@Singleton
class CommandController @Inject() (reminderService: ReminderService) extends Controller {
  
  def executeCommand = Action { request =>
    request.body.asFormUrlEncoded.map(SlashCommand(_)) match {
      case Some(slashCommand) =>
        reminderService.createReminder(slashCommand)
        Ok()
      case None =>
        BadRequest()
    }
  }

}
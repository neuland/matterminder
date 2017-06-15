package controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

@Singleton
class CommandController @Inject() extends Controller {
  
  def executeCommand = Action { request => 
    Logger.info("Parameters: " + request.body)
    
    
    
    
    Ok(Json.obj(
      "response_type" -> "in_channel",
      "text" -> "Läuft!",
      "username" -> "Matterminder"
    ))
  }

}

package de.neuland.services

import play.api.Logger
import javax.inject.Singleton

import com.typesafe.config.{Config, ConfigFactory}

@Singleton
class WebhookAuthenticationService {
  
  private val slashCommandTokenToWebhookKeyMap = createSlashCommandTokenToWebhookKeyMap(ConfigFactory.load("mattermost.conf"))
  
  def getWebhookKeyForCommandToken(commandToken: String): String = {
    slashCommandTokenToWebhookKeyMap.getOrElse(commandToken, "")
  }
  
  private def createSlashCommandTokenToWebhookKeyMap(config: Config): Map[String, String] = {
    config.getString("slashCommandTokensToWebhookKeys").split("#")
      .map(tokenToKeyMapping => tokenToKeyMapping.split(":"))
      .flatMap(splitToTokenAndKey)
      .toMap
  }
  
  private def splitToTokenAndKey(splitEntry: Array[String]): Option[Tuple2[String, String]] = {
    if (splitEntry != null && splitEntry.length == 2) 
      Option(splitEntry(0) -> splitEntry(1))
    else
      Logger.warn(s"Could not parse slash command token to webhook key mapping: $splitEntry")
      Option.empty
  }

}

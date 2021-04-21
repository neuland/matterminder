package de.neuland.services

import play.api.{Configuration, Logging}

import javax.inject.{Inject, Singleton}

@Singleton
class WebhookAuthenticationService @Inject()(config: Configuration) extends Logging {
  
  private val slashCommandTokenToWebhookKeyMap = createSlashCommandTokenToWebhookKeyMap
  
  def getWebhookKeyForCommandToken(commandToken: String): String = {
    slashCommandTokenToWebhookKeyMap.getOrElse(commandToken, "")
  }
  
  private def createSlashCommandTokenToWebhookKeyMap: Map[String, String] = {
    config
      .get[String]("mattermost.slashCommandTokensToWebhookKeys")
      .split("#")
      .flatMap(splitToTokenAndKey)
      .toMap
  }
  
  private def splitToTokenAndKey(mapping: String): Option[Tuple2[String, String]] = {
    val splitEntry = mapping.split(":")
    if (splitEntry != null && splitEntry.length == 2) {
      Option(splitEntry(0) -> splitEntry(1))
    } else {
      logger.warn(s"Could not parse slash command token to webhook key mapping: ${splitEntry.mkString("Array(", ", ", ")")}")
      Option.empty
    }
  }

}

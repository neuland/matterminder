package de.neuland.models

case class SlashCommand(
   token: String,
   teamId: String,
   teamDomain: String,
   channelId: String,
   channelName: String,
   userId: String,
   userName: String,
   command: String,
   text: String
 )

object SlashCommand {
  def apply(requestBody: Map[String, Seq[String]]): SlashCommand =
    SlashCommand(
      requestBody("token").head,
      requestBody("team_id").head,
      requestBody("team_domain").head,
      requestBody("channel_id").head,
      requestBody("channel_name").head,
      requestBody("user_id").head,
      requestBody("user_name").head,
      requestBody("command").head,
      requestBody("text").head
    )
}
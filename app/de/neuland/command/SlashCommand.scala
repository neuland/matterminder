package de.neuland.command

case class SlashCommand(
   token: String,
   teamId: String,
   teamDomain: String,
   channelId: String,
   channelName: String,
   userId: String,
   userName: String,
   commandType: String,
   command: String,
   text: String
 )

object SlashCommand {
  private val knownCommandTypes = Set("help", "create", "list", "delete")
  private val commandTypeMatcher = s"^(${knownCommandTypes.mkString("|")}) ?".r
  
  def apply(requestBody: Map[String, Seq[String]]): SlashCommand = {
    val command: CommandTypeAndCommand = splitCommandToCommandAndCommandType(requestBody("text").head)
    SlashCommand(
      requestBody("token").head,
      requestBody("team_id").head,
      requestBody("team_domain").head,
      requestBody("channel_id").head,
      requestBody("channel_name").head,
      requestBody("user_id").head,
      requestBody("user_name").head,
      command.commandType,
      command.command,
      removeLeadingCommandType(requestBody("text").head)
    )
  }

  private def splitCommandToCommandAndCommandType(commandFromRequestBody: String): CommandTypeAndCommand = {
    commandFromRequestBody match {
      case c if c.startsWith("help") => CommandTypeAndCommand("help", "")
      case c if c.startsWith("create") => CommandTypeAndCommand("create", removeLeadingCommandType(c))
      case c if c.startsWith("list") => CommandTypeAndCommand("list", removeLeadingCommandType(c))
      case c if c.startsWith("delete") => CommandTypeAndCommand("delete", removeLeadingCommandType(c))
      case c => CommandTypeAndCommand("help", "")
    }
  }
  
  private def removeLeadingCommandType(text: String): String = {
    commandTypeMatcher.replaceFirstIn(text, "")
  }
  
  private case class CommandTypeAndCommand(commandType: String, command: String)
}
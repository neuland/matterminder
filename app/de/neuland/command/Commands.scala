package de.neuland.command

object Commands {
  
  val commandHelpTexts = List(
    CommandHelp("help", "Prints this help text. Usage: /remind [help]"),
    CommandHelp("create", "Creates a new reminder. Whitespaces in names must be replaced by a '-'. Usage: /remind create (@USER|#CHANNEL) [that|to] \"don't forget to do something\" every [NUMBER] (weekday|monday|...) at (9:25(am|pm)|16:00)\n"
      + "Examples:\n"
      + "* /remind create #off-topic \"standup starts in 5 minutes!\" every weekday at 9:25\n"
      + "* /remind create #town-square \"don't forget to switch the lights off!\" every friday at 6:00pm"),
    CommandHelp("list", "Prints all reminders for this receiver. If no user or channel is given, the current one will be used. Usage: /remind list [USER|CHANNEL]"),
    CommandHelp("delete", "Deletes the specified reminder. Usage: /remind delete [REMINDER_ID]")
  )
  
  case class CommandHelp(command: String, help: String) {
    override def toString: String = {
      s":cyclone: **$command**\n$help"
    }
  }

}

package de.neuland.reminder.postgres

case class Reminder(id: String, author: String, recipient: String, message: String, schedules: String)

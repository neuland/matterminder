package de.neuland.parser

sealed trait Target
case object Me extends Target
case class User(name: String) extends Target
case class Channel(name: String) extends Target

sealed trait Interval
case object Day extends Interval
case object Week extends Interval

sealed trait CertainDay

case object AllWeekdays extends CertainDay

case object Monday extends CertainDay
case object Tuesday extends CertainDay
case object Wednesday extends CertainDay
case object Thursday extends CertainDay
case object Friday extends CertainDay
case object Saturday extends CertainDay
case object Sunday extends CertainDay

sealed trait Time
case class AbsoluteTime(time: java.time.LocalTime) extends Time
case object CurrentTime extends Time

sealed trait Schedule
case class EveryN(interval :Interval, n: Int, time: Time) extends Schedule
case class OnCertainDays(day: CertainDay, n: Int, time: Time) extends Schedule
case class OnceTodaySchedule(time: Time) extends Schedule

case class ParseResult(target: Target, message: String, schedules: Seq[Schedule])

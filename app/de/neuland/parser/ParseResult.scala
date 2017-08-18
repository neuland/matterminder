package de.neuland.parser

import java.time.{DayOfWeek, LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

import play.api.Logger

import scala.language.implicitConversions

sealed trait Target
case object Me extends Target
case class User(name: String) extends Target
case class Channel(name: String) extends Target

sealed trait Interval
case object Day extends Interval {
  override def toString = "days"
}
case object Week extends Interval {
  override def toString = "weeks"
}

sealed trait CertainDay {
  val toDaysOfWeek: List[DayOfWeek]
}
object CertainDay {
  def fromString(stringRepresentation: String): Option[CertainDay] = {
    stringRepresentation match {
      case AllWeekdays.toString => Option(AllWeekdays)
      case Monday.toString => Option(Monday)
      case Tuesday.toString => Option(Tuesday)
      case Wednesday.toString => Option(Wednesday)
      case Thursday.toString => Option(Thursday)
      case Friday.toString => Option(Friday)
      case Saturday.toString => Option(Saturday)
      case Sunday.toString => Option(Sunday)
      case _ =>
        Logger.warn(s"Could not parse CertainDay: $stringRepresentation")
        Option.empty
    }
  }
}

case object AllWeekdays extends CertainDay {
  override val toString: String = "allWeekdays"
  override val toDaysOfWeek = List(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
}

case object Monday extends CertainDay {
  override val toString: String = "mon"
  override val toDaysOfWeek = List(DayOfWeek.MONDAY)
}
case object Tuesday extends CertainDay {
  override val toString: String = "tue"
  override val toDaysOfWeek = List(DayOfWeek.TUESDAY)
}
case object Wednesday extends CertainDay {
  override val toString: String = "wed"
  override val toDaysOfWeek = List(DayOfWeek.WEDNESDAY)
}
case object Thursday extends CertainDay {
  override val toString: String = "thu"
  override val toDaysOfWeek = List(DayOfWeek.THURSDAY)
}
case object Friday extends CertainDay {
  override val toString: String = "fri"
  override val toDaysOfWeek = List(DayOfWeek.FRIDAY)
}
case object Saturday extends CertainDay {
  override val toString: String = "sat"
  override val toDaysOfWeek = List(DayOfWeek.SATURDAY)
}
case object Sunday extends CertainDay {
  override val toString: String = "sun"
  override val toDaysOfWeek = List(DayOfWeek.SUNDAY)
}



sealed trait Time {
  def getLocalTime: LocalTime
}
object Time {
  def fromString(stringRepresentation: String): Option[AbsoluteTime] = AbsoluteTime.fromString(stringRepresentation)
}
case class AbsoluteTime(time: LocalTime) extends Time {
  override def toString: String = AbsoluteTime.formatter.format(time)
  override def getLocalTime: LocalTime = time
}
object AbsoluteTime {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  def fromString(stringRepresentation: String): Option[AbsoluteTime] = {
    try {
      Option(AbsoluteTime(LocalTime.parse(stringRepresentation, formatter)))
    } catch {
      case _: Throwable =>
        Logger.warn(s"Could not parse AbsoluteTime: $stringRepresentation")
        Option.empty
    }
  }
}

sealed trait Schedule {
  def nextSchedule(from: LocalDateTime): Option[LocalDateTime]
  def adjustToTimeOfDay(from: LocalDateTime, timeToAdjustTo: Time): LocalDateTime = {
    val localTime = timeToAdjustTo.getLocalTime
    from.withHour(localTime.getHour).withMinute(localTime.getMinute).withSecond(localTime.getSecond)
  }
  implicit def secondsOfDay(localDateTime: LocalDateTime): HasSecondsOfDay = HasSecondsOfDay(localDateTime.getHour * 3600 + localDateTime.getMinute * 60 + localDateTime.getSecond)
  implicit def secondsOfDay(localTime: LocalTime): HasSecondsOfDay = HasSecondsOfDay(localTime.toSecondOfDay)
  
  case class HasSecondsOfDay(secondsOfDay: Int)
}

object Schedule {
  def fromString(stringRepresentation: String): Option[Schedule] = {
    stringRepresentation match {
//      case s if s.startsWith(EveryN.identifier) => EveryN.fromString(s)
      case s if s.startsWith(OnCertainDays.identifier) => OnCertainDays.fromString(s)
      case s if s.startsWith(OnceTodaySchedule.identifier) => OnceTodaySchedule.fromString(s)
      case _ =>
        Logger.warn(s"Could not parse Schedule: $stringRepresentation")
        Option.empty
    }
  }
} 

//case class EveryN(interval :Interval, n: Int, time: Time) extends Schedule {
//  override def toString = s"${EveryN.identifier}|$n|${interval.toString}|${time.toString}"
//}
//object EveryN {
//  val identifier = "every"
//  def fromString(stringRepresentation: String): Option[EveryN] = ???
//}


case class OnCertainDays(day: CertainDay, n: Int, time: Time) extends Schedule {
  override def toString = s"${OnCertainDays.identifier}|$n|${day.toString}|${time.toString}"

  override def nextSchedule(from: LocalDateTime): Option[LocalDateTime] = {
    val tooLateForToday = from.secondsOfDay >= time.getLocalTime.secondsOfDay
    Option(adjustToTimeOfDay(adjustToNextDay(from, day, tooLateForToday), time))
  }

  def adjustToNextDay(from: LocalDateTime, certainDay: CertainDay, tooLateForToday: Boolean): LocalDateTime = {
    certainDay.toDaysOfWeek.map(dayOfWeek => adjustToNextDay(from, dayOfWeek, tooLateForToday)).reduce((left, right) => if(left.isAfter(right)) right else left)
  }
  def adjustToNextDay(from: LocalDateTime, dayOfWeek: DayOfWeek, tooLateForToday: Boolean): LocalDateTime = {
    if(tooLateForToday)
      from.`with`(TemporalAdjusters.next(dayOfWeek))
    else
      from.`with`(TemporalAdjusters.nextOrSame(dayOfWeek))
  }
}
object OnCertainDays {
  val identifier = "on"
  def fromString(stringRepresentation: String): Option[OnCertainDays] = {
    val splittedRepresentation = stringRepresentation.split("\\|")
    if(splittedRepresentation.length == 4) {
      val number = toNumber(splittedRepresentation(1))
      if(number.isEmpty) {
        return Option.empty
      }
      val maybeDay = CertainDay.fromString(splittedRepresentation(2))
      if(maybeDay.isEmpty) {
        return Option.empty
      }
      val maybeTime = Time.fromString(splittedRepresentation(3))
      maybeTime.map(time => OnCertainDays(maybeDay.get, number.get, time))
      
    } else {
      Logger.warn(s"Could not parse OnCertainDays: $stringRepresentation")
      Option.empty
    }
  }

  private def toNumber(stringRepresentation: String): Option[Int] = {
    try {
      Option(stringRepresentation.toInt)
    } catch {
      case _: Throwable =>
        Logger.warn(s"Could not parse number: $stringRepresentation")
        Option.empty
    }
  }
}


case class OnceTodaySchedule(time: Time) extends Schedule {
  override def toString = s"${OnceTodaySchedule.identifier}|${time.toString}"
  override def nextSchedule(from: LocalDateTime): Option[LocalDateTime] = {
    val tooLateForToday = from.secondsOfDay >= time.getLocalTime.secondsOfDay
    if(tooLateForToday) {
      Option.empty
    } else {
      Option(adjustToTimeOfDay(from, time))
    }
  }
}
object OnceTodaySchedule {
  val identifier = "once"
  def fromString(stringRepresentation: String): Option[OnceTodaySchedule] = {
    val splittedRepresentation = stringRepresentation.split("\\|")
    if(splittedRepresentation.length == 2) {
      Time.fromString(splittedRepresentation(1)).map(OnceTodaySchedule.apply)
    } else {
      Logger.warn(s"Could not parse OnceToday: $stringRepresentation")
      Option.empty
    }
  }
}

case class ParseResult(target: Target, message: String, schedules: Seq[Schedule])

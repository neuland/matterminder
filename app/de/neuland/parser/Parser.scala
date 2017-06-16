package de.neuland.parser

import java.time.LocalTime

import fastparse.all
import fastparse.all._

class Parser {
  val anyWhitespace = P(CharPred(_.isWhitespace).rep)
  val word = P(CharPred(c => !c.isWhitespace && !c.isControl).rep(1))
  val wordSep = P(CharPred(c => c.isWhitespace || c.isControl).rep(1) | End)

  private def keyword(keyword: String): fastparse.all.Parser[Unit] = {
    P(IgnoreCase(keyword) ~ wordSep)
  }

  private def interval(interval: String): fastparse.all.Parser[String] = {
    P(StringInIgnoreCase(interval, interval + "s").! ~ wordSep)
  }

  private def numberWord(word: String, number: Int): fastparse.all.Parser[Int] = {
    P(IgnoreCase(word).map(_ => number) ~ wordSep)
  }

  val that = keyword("that")
  val to = keyword("to")

  val at = keyword("at")
  val every = keyword("every")
  val on = keyword("on")
  val in = keyword("in")

  val day = interval("day").map(_ => Day)
  val week = interval("week").map(_ => Week)
  val weekday = interval("weekday").map(_ => AllWeekdays)
  val monday = interval("monday").map(_ => Monday)
  val tuesday = interval("tuesday").map(_ => Tuesday)
  val wednesday = interval("wednesday").map(_ => Wednesday)
  val thursday = interval("thursday").map(_ => Thursday)
  val friday = interval("friday").map(_ => Friday)
  val saturday = interval("saturday").map(_ => Saturday)
  val sunday = interval("sunday").map(_ => Sunday)

  val weekdays: all.Parser[CertainDay] = P(monday | tuesday | wednesday | thursday | friday | saturday | sunday)

  val digit = P(CharPred(_.isDigit).!)
  val number = P(digit.rep(1).!.map(_.toInt) ~ wordSep)

  val upToTwoDigits = P(digit.rep(min=1, max=2).!)
  val twoDigits = P((digit ~ digit).!)
  val time_12: all.Parser[Time] = P(upToTwoDigits ~ (":" ~ twoDigits).? ~ ("am" | "pm").! ~ wordSep) map {
    case (hours, Some(minutes), "am") => AbsoluteTime(LocalTime.of(hours.toInt, minutes.toInt))
    case (hours, None, "am") => AbsoluteTime(LocalTime.of(hours.toInt, 0))
    case (hours, Some(minutes), "pm") => AbsoluteTime(LocalTime.of(hours.toInt + 12, minutes.toInt))
    case (hours, None, "pm") => AbsoluteTime(LocalTime.of(hours.toInt + 12, 0))
  }
  val time_24: all.Parser[Time] = P(upToTwoDigits ~ ":" ~ twoDigits ~ wordSep) map {
    case (hours, minutes) => AbsoluteTime(LocalTime.of(hours.toInt, minutes.toInt))
  }

  val zero = numberWord("zero", 0)
  val one = numberWord("one", 1)
  val two = numberWord("two", 2)

  val numberThing: fastparse.all.Parser[Int] = P(number | one | two | zero)

  val intervalThing: all.Parser[(Int, Interval)] = P(every ~ numberThing.?.map(_.getOrElse(1)) ~ (day | week))
  val certainDays: all.Parser[(Int, CertainDay)] = P(every ~ numberThing.?.map(_.getOrElse(1)) ~ (weekday | weekdays))

  /*val intervalThing: all.Parser[(Option[Int], Either[Interval, CertainDay])] = P(
    every ~ numberThing.? ~ (
      (day | week).map(Left(_)) | (weekday | weekdays).map(Right(_))
    ))*/

  val timeThing: fastparse.all.Parser[Time] = P(at ~ (time_12 | time_24))

  val me: fastparse.all.Parser[Target] = P(IgnoreCase("me").! ~ wordSep).map(_ => Me)
  val user: fastparse.all.Parser[Target] = P("@" ~ word.! ~ wordSep).map(User)
  val channel: fastparse.all.Parser[Target] = P("#" ~ word.! ~ wordSep).map(Channel)
  val target: fastparse.all.Parser[Target] = P(me | user | channel)

  val quotedString: all.Parser[String] = P("\"" ~ CharPred(_ != '"').rep.! ~ "\"" ~ anyWhitespace)

  val message: all.Parser[String] = P((that | to).? ~ quotedString)

  val test: all.Parser[(Int, CertainDay, Time)] = P(certainDays ~ timeThing)

  val onCertainDays: all.Parser[Schedule] = P(
    //certainDays.map((n: Int, days: CertainDay) => OnCertainDays(days, n, CurrentTime))
    (certainDays ~ timeThing).map {
      case (n: Int, days: CertainDay, time: Time) => OnCertainDays(days, n, time)
    }
  )

  val reminder: all.Parser[ParseResult] = P(Start ~ target ~ message ~ onCertainDays).map {
    case (target_, message_, schedule) => ParseResult(target_, message_, Seq(schedule))
  }
}

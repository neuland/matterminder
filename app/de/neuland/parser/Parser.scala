package de.neuland.parser

import java.time.LocalTime

import fastparse._, NoWhitespace._

class Parser {
  private def day[_: P] = P(StringInIgnoreCase("day", "days").! ~ wordSep).map(_ => Day)
  private def week[_: P] = P(StringInIgnoreCase("week", "weeks").! ~ wordSep).map(_ => Week)
  private def weekday[_: P] = P(StringInIgnoreCase("weekday", "weekdays").! ~ wordSep).map(_ => AllWeekdays)
  private def monday[_: P] = P(StringInIgnoreCase("monday", "mondays").! ~ wordSep).map(_ => Monday)
  private def tuesday[_: P] = P(StringInIgnoreCase("tuesday", "tuesdays").! ~ wordSep).map(_ => Tuesday)
  private def wednesday[_: P] = P(StringInIgnoreCase("wednesday", "wednesdays").! ~ wordSep).map(_ => Wednesday)
  private def thursday[_: P] = P(StringInIgnoreCase("thursday", "thursdays").! ~ wordSep).map(_ => Thursday)
  private def friday[_: P] = P(StringInIgnoreCase("friday", "fridays").! ~ wordSep).map(_ => Friday)
  private def saturday[_: P] = P(StringInIgnoreCase("saturday", "saturdays").! ~ wordSep).map(_ => Saturday)
  private def sunday[_: P] = P(StringInIgnoreCase("sunday", "sundays").! ~ wordSep).map(_ => Sunday)
  private def weekdays[_: P] = P(monday | tuesday | wednesday | thursday | friday | saturday | sunday)

  private def anyWhitespace[_: P] = P(CharPred(_.isWhitespace).rep)
  private def upToTwoDigits[_: P] = P(digit.rep(min=1, max=2).!)
  private def twoDigits[_: P] = P((digit ~ digit).!)
  private def at[_: P] = P(IgnoreCase("at") ~ wordSep)
  private def timeThing[_: P] = P(at ~ (time_12 | time_24))
  private def time_24[_: P] = P(upToTwoDigits ~ ":" ~ twoDigits ~ wordSep) map {
    case (hours, minutes) => AbsoluteTime(LocalTime.of(hours.toInt, minutes.toInt))
  }
  private def am[_: P] = P(StringInIgnoreCase("am")).map(_ => Am)
  private def pm[_: P] = P(StringInIgnoreCase("pm")).map(_ => Pm)
  private def daytime[_: P] = P(am | pm)
  private def time_12[_: P] = P(upToTwoDigits ~ (":" ~ twoDigits).? ~ anyWhitespace.? ~ daytime ~ wordSep) map {
    case (hours, Some(minutes), Am) => AbsoluteTime(LocalTime.of(hours.toInt, minutes.toInt))
    case (hours, None, Am) => AbsoluteTime(LocalTime.of(hours.toInt, 0))
    case (hours, Some(minutes), Pm) => AbsoluteTime(LocalTime.of(hours.toInt + 12, minutes.toInt))
    case (hours, None, Pm) => AbsoluteTime(LocalTime.of(hours.toInt + 12, 0))
  }

  private def numberWord[_: P](word: String, number: Int) = {
    P(IgnoreCase(word).map(_ => number) ~ wordSep)
  }
  private def zero[_: P] = numberWord("zero", 0)
  private def one[_: P] = numberWord("one", 1)
  private def two[_: P] = numberWord("two", 2)
  private def digit[_: P] = P(CharPred(_.isDigit).!)
  private def number[_: P] = P(digit.rep(1).!.map(_.toInt) ~ wordSep)
  private def numberThing[_: P] = P(number | one | two | zero)
  private def every[_: P] = P(IgnoreCase("every") ~ wordSep)
  private def certainDays[_: P] = P(every ~ numberThing.?.map(_.getOrElse(1)) ~ (weekday | weekdays))
  private def onCertainDays[_: P] = P(
    //certainDays.map((n: Int, days: CertainDay) => OnCertainDays(days, n, CurrentTime))
    (certainDays ~ timeThing).map {
      case (n: Int, days: CertainDay, time: Time) => OnCertainDays(days, n, time)
    }
  )

  private def quotedString[_: P] = P("\"" ~ CharPred(_ != '"').rep.! ~ "\"" ~ anyWhitespace)

  private def that[_: P] = P(IgnoreCase("that") ~ wordSep)
  private def to[_: P] = P(IgnoreCase("to") ~ wordSep)
  private def message[_: P] = P((that | to).? ~ quotedString)

  private def wordSep[_: P] = P(CharPred(c => c.isWhitespace || c.isControl).rep(1) | End)
  private def word[_: P] = P(CharPred(c => !c.isWhitespace && !c.isControl).rep(1))

  private def me[_: P] = P(IgnoreCase("me").! ~ wordSep).map(_ => Me)
  private def user[_: P] = P("@" ~ word.! ~ wordSep).map(User)
  private def channel[_: P] = P("#" ~ word.! ~ wordSep).map(Channel)
  private def target[_: P] = P(me | user | channel)
  private def reminder[_: P] = P(Start ~ target ~ message ~ onCertainDays).map {
    case (_target, _message, _schedule) => ParseResult(_target, _message, Seq(_schedule))
  }

  def parseReminder(reminderString: String): Parsed[ParseResult] = {
    parse(reminderString, reminder(_))
  }
}

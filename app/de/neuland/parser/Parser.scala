package de.neuland.parser

import fastparse.all._

class Parser {
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

  val day = interval("day")
  val week = interval("week")
  val weekday = interval("weekday")
  val monday = interval("monday")
  val tuesday = interval("tuesday")
  val wednesday = interval("wednesday")
  val thursday = interval("thursday")
  val friday = interval("friday")
  val saturday = interval("saturday")
  val sunday = interval("sunday")

  val weekdays = P(monday | tuesday | wednesday | thursday | friday | saturday | sunday)

  val digit = P(CharPred(_.isDigit).!)
  val number = P(digit.rep(1).!.map(_.toInt) ~ wordSep)

  val upToTwoDigits = P(digit.rep(min=1, max=2).!)
  val twoDigits = P((digit ~ digit).!)
  val time_12 = P(upToTwoDigits.! ~ (":" ~ twoDigits.!).? ~ ("am" | "pm").! ~ wordSep)
  val time_24 = P(upToTwoDigits.! ~ ":" ~ twoDigits.! ~ wordSep)

  val zero = numberWord("zero", 0)
  val one = numberWord("one", 1)
  val two = numberWord("two", 2)

  val numberThing: fastparse.all.Parser[Int] = P(number | one | two | zero)


  val intervalThing = P(every ~ numberThing.? ~ (day | week | weekday).!)

  val timeThing = P(at ~ (time_12 | time_24).!)

  val me = P(IgnoreCase("me").! ~ wordSep)
  val user = P(("@" ~ word).! ~ wordSep)
  val channel = P(("#" ~ word).! ~ wordSep)
  val target = P(me | user | channel)

  val quotedString = P("\"" ~ CharPred(_ != '"').rep.! ~ "\"")

  val message = P((that | to).? ~ ((word ~ wordSep).rep(1).! | quotedString.!))


  val timespec = P(intervalThing)


  val basicTest = P(Start ~ target ~ message)
}

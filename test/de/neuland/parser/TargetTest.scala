package de.neuland.parser

import fastparse.all._
import org.scalatest.FunSuite

class BaseParserTest extends FunSuite {
  protected val parser = new Parser()
}

class TargetTest extends BaseParserTest {
  test("should parse me") {
    val Parsed.Success("me", 5) = parser.target.parse("me   ")
  }

  test("should not parse me2") {
    val Parsed.Failure(_, _, _) = parser.target.parse("me2")
  }

  test("should parse a user name") {
    val Parsed.Success("@testuser25", _) = parser.target.parse("@testuser25")
  }

  test("should parse a channel name") {
    val Parsed.Success("#test-channel_5", _) = parser.target.parse("#test-channel_5  ")
  }

  test("should parse all-caps ME") {
    val Parsed.Success("ME", _) = parser.target.parse("ME")
  }
}

class QuotedStringTest extends BaseParserTest {
  test("should parse quoted string") {
    val Parsed.Success("quoted string", _) = parser.quotedString.parse("\"quoted string\"")
  }

  test("should not parse unbalanced quotes") {
    val Parsed.Failure(_, _, _) = parser.quotedString.parse("\"unbalanced quotes")
  }
}

class NumberTest extends BaseParserTest {
  test("should parse numbers") {
    val Parsed.Success(5, _) = parser.numberThing.parse("5")
  }

  test("should parse number word") {
    val Parsed.Success(1, _) = parser.numberThing.parse("one   ")
  }
}

class IntervalTest extends BaseParserTest {
  test("should parse interval") {
    val Parsed.Success((Some(5), "weeks"), _) = parser.intervalThing.parse("every 5 weeks")
  }

  test("should parse other interval") {
    val Parsed.Success((None, "weekday"), _) = parser.intervalThing.parse("every weekday")
  }

  test("should parse interval with number word") {
    val Parsed.Success((Some(2), "days"), _) = parser.intervalThing.parse("every two days")
  }
}

class TimestampTest extends BaseParserTest {
  test("should parse 24-hour time") {
    val Parsed.Success("11:45", _) = parser.timeThing.parse("at 11:45")
  }

  test("should parse 12-hour am time - 1 digit") {
    val Parsed.Success("9am", _) = parser.timeThing.parse("at 9am")
  }

  test("should parse 12-hour time - 2 digits") {
    val Parsed.Success("11pm", _) = parser.timeThing.parse("at 11pm")
  }

  test("should parse 12-hour time - minutes") {
    val Parsed.Success("9:25am", _) = parser.timeThing.parse("at 9:25am")
  }
}

class ParserTest extends BaseParserTest {
  test("should parse basic reminder with 'that'") {
    val Parsed.Success(("me", "everything is fine"), _) = parser.basicTest.parse("me that everything is fine")
  }

  test("should parse basic reminder without 'that'") {
    val Parsed.Success(("me", "everything is fine  "), _) = parser.basicTest.parse("me    everything is fine  ")
  }

  test("should parse basic reminder with 'to'") {
    val Parsed.Success(("#channel1", "do stuff"), _) = parser.basicTest.parse("#channel1 to do stuff")
  }

  test("should only eat one filler word") {
    val Parsed.Success(("me", "that do stuff"), _) = parser.basicTest.parse("me to that do stuff")
  }

  test("should parse basic reminder for user") {
    val Parsed.Success(("@batman", "he can breathe in space"), _) = parser.basicTest.parse("@batman that he can breathe in space")
  }

  test("should parse double-quoted string") {
    val Parsed.Success(("#channel1", "\"don't scare the bear\""), _) = parser.basicTest.parse("#channel1 \"don\'t scare the bear\"")
  }
}

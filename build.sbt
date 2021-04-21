name := """matterminder"""
organization := "de.neuland"

version := "1.0.0-RC01"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.5"

libraryDependencies += filters

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.2.19",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.lihaoyi" %% "fastparse" % "2.3.2",
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)

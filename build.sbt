name := """matterminder"""
organization := "de.neuland"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += filters

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"
libraryDependencies += "com.typesafe.slick" %% "slick" % "2.1.0"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.3"

libraryDependencies += ws

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "de.neuland.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "de.neuland.binders._"

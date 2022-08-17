name := "akka-essentials"

version := "0.1"

scalaVersion := "2.13.8"

val akkaVersion="2.9.16"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.23"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.23"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12"
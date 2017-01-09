name := "akka-streams"

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.4.16"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.22",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion exclude("log4j", "log4j"),
  "com.typesafe.akka" %% "akka-http" % "10.0.1"
)
    
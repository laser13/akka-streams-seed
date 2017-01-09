package ru.pavlenov.http

import akka.actor.ActorSystem
import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.stream.ActorMaterializer

import scala.io.StdIn
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * ⓭ + 52
  * Какой сам? by Pavlenov Semen 02.01.17.
  */
class Server(implicit system: ActorSystem, materializer: ActorMaterializer) {

  val log = Logging(system, this.getClass)

  def akkaResponseTimeLoggingFunction(
    loggingAdapter:   LoggingAdapter,
    requestTimestamp: Long,
    level:            LogLevel       = Logging.InfoLevel)(req: HttpRequest)(res: Any): Unit = {
    val entry = res match {
      case Complete(resp) =>
        val responseTimestamp: Long = System.nanoTime
        val elapsedTime: Long = (responseTimestamp - requestTimestamp) / 1000000
        val loggingString =
          s"""
             |Complete: ${req.method}:${req.uri} -> ${resp.status}:${elapsedTime}ms.
             |${req}
             |${resp}""".stripMargin
        LogEntry(loggingString, level)
      case Rejected(reason) =>
        LogEntry(s"Rejected Reason: ${reason.mkString(",")}", level)
    }
    entry.logTo(loggingAdapter)
  }
  def printResponseTime(log: LoggingAdapter): (HttpRequest) => (Any) => Unit = {
    val requestTimestamp = System.nanoTime
    akkaResponseTimeLoggingFunction(log, requestTimestamp)(_)
  }

  val logResponseTime: Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(printResponseTime))

  val route =
    path("hello") {
      get {
        logResponseTime(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

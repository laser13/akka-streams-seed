package ru.pavlenov

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import ru.pavlenov.http.Server
import ru.pavlenov.streams.Simple

import scala.collection.immutable.TreeMap
import scala.collection.mutable

/**
  * ⓭ + 26
  * Какой сам? by Pavlenov Semen 01.01.17.
  */
object Main extends App{

  implicit val system: ActorSystem = ActorSystem("QuickStart")

  val decider: Supervision.Decider = {
    case _ => Supervision.Resume
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider),
    "MyMat")

  val log = Logging(system, this.getClass)
  log.info("Start Akka Streams Examples")

  val simple = new Simple()
  simple.test1.runWith(Sink.ignore)
//  simple.test2.run()

//  val server = new Server()

  system.terminate()

}

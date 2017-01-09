package ru.pavlenov.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl._

/**
  * ⓭ + 34
  * Какой сам? by Pavlenov Semen 01.01.17.
  */
class Simple(implicit system: ActorSystem, materializer: ActorMaterializer) {

  val log = Logging(system, this.getClass)

  val source: Source[Int, NotUsed] = Source(1 to 20)
    .log("source")
    .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
    .named("Range[1-20]")

  val help: Flow[Any, Any, NotUsed] = Flow[Any]
    .log("finish")
    .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))

  def test1 = {
    Source.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bc = b.add(Broadcast[Int](2))
      val isEven = b.add(Flow[Int].map(_ % 2 == 0)
        .log("isEven").withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel)))
      val zip = b.add(Zip[Int, Boolean])
      val output = b.add(Flow[(Int, Boolean)]
        .log("output").withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel)))
      val part = b.add(Partition[(Int, Boolean)](2, {
        case (_, true) => 1
        case (_, false) => 0
      }))
      val finish = b.add(help)

      source ~> bc
                bc ~>           zip.in0
                bc ~> isEven ~> zip.in1
                                zip.out ~> output ~> part
                                                     part.out(0) ~> Sink.ignore
                                                     part.out(1) ~> finish

      SourceShape(finish.out)
    })
  }

  def test2 = {

      source.groupBy(2, _ > 10)
        .map { x =>
          println(x)
          x
        }
        .log("group")
        .map {
          case i if i > 10 => i / 2
          case i => i + 1
        }
        .log("action")
        .mergeSubstreams
        .to(Sink.ignore)

  }

}

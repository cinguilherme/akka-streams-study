package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.scaladsl.GraphDSL.Implicits._

object OpenGraphs extends App {


  implicit val system = ActorSystem("openGraphs")
  implicit val meterializer = ActorMaterializer()

  val sourceOne = Source(1 to 10)
  val sourceTwo = Source(42 to 200)

  val complexSource = Source.fromGraph(
    GraphDSL.create() {implicit  builder =>

      val concat = builder.add(Concat[Int](2))

      sourceOne ~> concat
      sourceTwo ~> concat

      SourceShape(concat.out)
    }
  )

  //complexSource.to(Sink.foreach[Int](println)).run()

  val sink1 = Sink.foreach[Int](i => println(s"this is something $i"))
  val sink2 = Sink.reduce[Int]((f, s) =>{println(s"reducing $f and $s"); f+s} )
  // one sink to many sinks
  val complexSink = Sink.fromGraph(
    GraphDSL.create() { implicit builder =>

      val spread = builder.add(Broadcast[Int](2))

      spread ~> sink1
      spread ~> sink2

      SinkShape(spread.in)
    }
  )

//  complexSource.to(complexSink).run()

  /**
    * build a complexFlow
    */
  val flow1 = Flow[Int].map(_ + 1)
  val flow2 = Flow[Int].map(_ * 2)
  val complexFlow = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>

      val outbound = builder.add(flow2)
      val inbound = builder.add(flow1)

      inbound ~> outbound

      FlowShape(inbound.in, outbound.out)
    }
  )

  complexSource.via(complexFlow).to(Sink.foreach[Int](println)).run()

  /**
    * Exercice, is it possible to create a Flow
    * from a Source and a Sink ?
    * Its possible but it is messy and messed up
    */

  val att = Flow.fromGraph(GraphDSL.create() { implicit builder =>

    val source = builder.add(Source(1 to 10))
    val sink = builder.add(Sink.reduce[Int](_ + _))


    FlowShape(sink.in, source.out)
  })

}

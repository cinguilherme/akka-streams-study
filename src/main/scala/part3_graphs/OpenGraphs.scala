package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, SinkShape, SourceShape}
import akka.stream.scaladsl.{Broadcast, Concat, GraphDSL, Sink, Source}
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
    *
    */

}

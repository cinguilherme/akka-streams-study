package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source, ZipWith}

object MoreOpenGraphs extends App {

  import GraphDSL.Implicits._
  implicit val system = ActorSystem("moreOpenGraphs")
  implicit val materializer = ActorMaterializer()

  /**
    * Max 3 operator
    */

  val max3StaticGraph = GraphDSL.create() {
    implicit builder =>


      val zip1 = builder.add(ZipWith[Int, Int, Int]((a,b) => Math.max(a, b)))
      val zip2 = builder.add(ZipWith[Int, Int, Int]((c,d) => Math.max(c, d)))

      zip1.out ~> zip2.in0

      UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
    }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(_ => 5)
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](println)

  val max3 = RunnableGraph.fromGraph(
    GraphDSL.create() {implicit builder =>

      val max3Shape = builder.add(max3StaticGraph)

      source1 ~> max3Shape.in(0)
      source2 ~> max3Shape.in(1)
      source3 ~> max3Shape.in(2)

      max3Shape.out ~> maxSink

      ClosedShape
    }
  )

//  max3.run()

  /**
    * Suspicious transaction detection component
    */

}

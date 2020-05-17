package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}

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
    * - NON uniform fanOut Shape
    * stream component for Trx type
    * - output 1 -> unmodified Trx pass
    * - output 2 -> trx ID as String to be further analized
    */

  case class Transation(id: String, ammount: BigDecimal)

  val list = List(
    Transation("1", 10000),
    Transation("2", 500),
    Transation("3", 20000))

  val trxSource = Source(list)

  val regularSink = Sink.foreach[Transation](t => println(s"regular sink, transacion comming through $t"))
  val suspiciousSink = Sink.foreach[String](id => println(s"this transaction is suspissious, ID $id"))

  val f = RunnableGraph.fromGraph( GraphDSL.create() {
    implicit builder =>


      val broadcast = builder.add(Broadcast[Transation](2))

      trxSource ~> broadcast

      broadcast.out(0) ~> regularSink

      val filterFlow = builder.add(Flow[Transation].filter(t => t.ammount > 9999).map(t => t.id))

      broadcast.out(1) ~> filterFlow ~> suspiciousSink

      ClosedShape
  })
  f.run()



}

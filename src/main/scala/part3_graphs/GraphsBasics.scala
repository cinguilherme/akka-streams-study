package part3_graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

object GraphsBasics extends App {

  implicit val system = ActorSystem("graphs")
  implicit val materializer = ActorMaterializer()


  val input = Source(1 to 1000)

  val output = Sink.foreach[(Int, Int)](println)

  val incrementer = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_ * 10)

  val complexGraph = RunnableGraph
    .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

      //MUTABLE BUILDER

      // create necessary components for the Graph
      val broadCast = builder.add(Broadcast[Int](2))
      val zip = builder.add(Zip[Int, Int])

      input ~> broadCast
      broadCast.out(0) ~> incrementer ~> zip.in0
      broadCast.out(1) ~> multiplier ~> zip.in1

      zip.out ~> output

      ClosedShape // FREEZE BUILDER
  })

  //complexGraph.run()

  /**
    * exercice 1 - feed 1 source to 2 different sinks
    */
  val sinkOne = Sink.foreach[Int]((in) => println(s"this is the sinkOne $in"))
  val sinkTwo = Sink.foreach[Int]((in) => println(s"this is the sinkTwo $in"))
  val graphOneSourceToTwoSinks = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val broadCast = builder.add(Broadcast[Int](2))

      input ~> broadCast
      broadCast.out(0) ~> sinkOne
      broadCast.out(1) ~> sinkTwo

      ClosedShape
  })

  graphOneSourceToTwoSinks.run()

}

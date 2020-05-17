package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.Success

object GraphMaterializedValues extends App {

  import GraphDSL.Implicits._
  implicit val system = ActorSystem("graphMaterializedValues")
  implicit val materializer = ActorMaterializer()

  val wordSource = Source(List("Scala", "Akka", "DSL", "Rocks", "single", "lowercase"))
  val printer = Sink.foreach(println)
  val counter = Sink.fold[Int, String](0)((count, _) => count +1)

  /**
    * Create a composite component (sink)
    * 1 - prints all strings that are lowercased
    * 2 - count strings that are shorter than 5 chars
    */
  val tg = GraphDSL.create(counter) {implicit  builder => counterShape =>

    val broadcast = builder.add(Broadcast[String](2))
    val flowLowCase = builder.add(Flow[String].filter(s => s.compareTo(s.toLowerCase()) == 0))
    val flowFilterLong = builder.add(Flow[String].filter(s => s.length < 6))

    wordSource ~> broadcast
    broadcast ~> flowLowCase ~> printer
    broadcast ~> flowFilterLong ~> counterShape

    ClosedShape
  }
  import system.dispatcher
  val rg = RunnableGraph.fromGraph(tg).run().onComplete {
    case Success(sum) => println(s"success calculation of words -> $sum")
    case _ => println("something odd happened ")
  }

  /**
    * Enhance Flow
    * create a composite component
    * Hint -> use a broadcast and a Sink.fold
    */


  def enhanceFlow[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = {
    val sinkFold = Sink.fold[Int, B](0)((count, _) => count + 1)
    Flow.fromGraph(
      GraphDSL.create(sinkFold) { implicit builder => sinkFoldShape =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[B](2))
      val originalFlowShape = builder.add(flow)

      originalFlowShape ~> broadcast ~> sinkFoldShape

      FlowShape(originalFlowShape.in, broadcast.out(1))
    })
  }

  val simpleFlow = Flow[Int].map(x => x)
  val simpleSource = Source(1 to 42)
  val ignoreSink = Sink.ignore

  val future = simpleSource
    .viaMat(enhanceFlow(simpleFlow))(Keep.right)
    .toMat(ignoreSink)(Keep.left).run()

  future.onComplete {
    case Success(count) => println(s"a total of $count elements passed via into EnhancedFlow")
  }

}

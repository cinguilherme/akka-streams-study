package part3_graphs

import java.awt.datatransfer.StringSelection

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import com.sun.tools.javac.util.StringUtils

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

}

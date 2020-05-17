package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}
import akka.stream.scaladsl.{BidiFlow, Flow, GraphDSL, RunnableGraph, Sink, Source}

object BidirectionalFlow extends App {

  implicit val system = ActorSystem("bidirectionalFlowSystem")
  implicit val materializer = ActorMaterializer()

  /**
    * Example Cryptografy or comom base64?
    * Cesar Cript
    */

  def encript(n: Int)(str: String): String = str.map(c => (c + n).toChar)
  def dencript(n: Int)(str: String): String = str.map(c => (c - n).toChar)

  val key = 3
  val encipted = encript(key)("You are not smart")
  val decripted = dencript(key)(encipted)
  println(encipted)
  println(decripted)

  val bidiFlow = GraphDSL.create() { implicit builder =>
    val encriptFlowShape = builder.add(Flow[String].map(encript(3)))
    val decriptFlowShape = builder.add(Flow[String].map(dencript(3)))

    BidiShape.fromFlows(encriptFlowShape, decriptFlowShape)
  }

  val list = List("Akka", "is", "cool")
  val sourceDecripted = Source(list)
  val sourceEncripted = Source(list.map(encript(3)))
  val encriptedSink = Sink.foreach[String](println)

  val encriptionGraph = RunnableGraph.fromGraph(GraphDSL.create() {implicit builder =>
    import GraphDSL.Implicits._

    val bibiShape = builder.add(bidiFlow)
    sourceDecripted ~> bibiShape.in1 ~> encriptedSink

    ClosedShape
  })




}

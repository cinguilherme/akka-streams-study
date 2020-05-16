package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.scaladsl._

object OperatorFusion extends App {

  implicit val system  = ActorSystem("operatorFusion")
  implicit val materializer = ActorMaterializer()

  val simpleSource = Source(1 to 1000)

  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  // THIS RUNS IN THE SAME ACTOR
  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
  // Operator/component FUSION

  val complexFlow = Flow[Int].map { x =>
    Thread.sleep(500)
    x + 1
  }

  val complexFlow2 = Flow[Int].map { x =>
    Thread.sleep(500)
    x * 10
  }

  // Operator/component FUSION is not efficient when expensive operations are in the stream pipeline
  simpleSource
    .via(complexFlow) //same actor + 500ms
    .via(complexFlow2) //same actor + 500ms
    .to(Sink.foreach[Int](i => println(s"simple pipe $i"))).run() //total of 1s to each output and this scales linearly

  //to break the FUSION the stream require the async operator to BREAK the fusion and use other actors
  simpleSource
    .via(complexFlow).async // another actor
    .via(complexFlow2).async // another actor
    .to(Sink.foreach[Int](i => println(s"fusion broken pipeline $i"))).run() // this should be twice as fast give or take

}

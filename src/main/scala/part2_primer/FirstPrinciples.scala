package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object FirstPrinciples extends App {

  implicit val system = ActorSystem("firstPrinciples")
  implicit val materializer = ActorMaterializer()

  //source, Upstream
  val source = Source(1 to 10)

  //sink
  val sink = Sink.foreach[Int](println)


  //definition of a akka stream
  val graph = source.to(sink)


  graph.run()
}

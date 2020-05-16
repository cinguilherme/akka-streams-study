package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackPressureBasics extends App {

  implicit val system = ActorSystem("backpressurebasics")
  implicit val materializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)

  val slowSink = Sink.foreach[Int] { x =>
    Thread.sleep(500)
    println(s"Slow sink $x")
  }

  //fastSource.to(slowSink).run() // this is Fusion, not back pressure

  //fastSource.async.to(slowSink).run() // backpressure is in place, we have a new actor for the sink.. pero no mucho

  val simpleFlow = Flow[Int].map {x => println(s"incoming $x"); x+1}

  // this here is awesome. back pressure at its finest
  fastSource.async
    .via(simpleFlow).async
    .to(slowSink)
    .run()

  /**
    * How this components reacts to back pressure?
    */
}

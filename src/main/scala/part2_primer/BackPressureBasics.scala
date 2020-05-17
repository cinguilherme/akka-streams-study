package part2_primer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
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

  import scala.concurrent.duration._

  val simpleFlow = Flow[Int].map {x => println(s"incoming $x"); x+1}

  val bffFlow = Flow[Int].buffer(10, OverflowStrategy.backpressure)
    .map(x => {println(s"intake on buffered $x"); x})


  val simpleFlow2 = Flow[Int].throttle(10, 1 second)
    .map(x => { println(s"intake on the simpleFlow 2 $x"); x + 1})

  // this here is awesome. back pressure at its finest
  fastSource.async
  .via(simpleFlow).async
  .to(slowSink)
  //    .run()

  /**
    * How this components reacts to back pressure?
    */
  fastSource.throttle(2, 1 second).to(Sink.foreach(println))
//    .run() //manual backpressure using throtle

  val retrySink = Sink.queue()

  fastSource
    .throttle(100, 1 second)
    .via(bffFlow).async
    .via(simpleFlow2).async
    .to(slowSink)
    .run()
}

package playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.javadsl.{Sink, Source}

object PlayGround extends App{

  println("running")

  implicit val actorSystem = ActorSystem("playground")
  implicit val materializer = ActorMaterializer()

  Source.single("hello, streams").to(Sink.foreach(println)).run(materializer)

}

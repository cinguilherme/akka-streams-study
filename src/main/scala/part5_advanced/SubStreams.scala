package part5_advanced

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object SubStreams extends App {

  implicit val system = ActorSystem("subStreams")
  implicit val materializer = ActorMaterializer()



}

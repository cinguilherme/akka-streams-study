package part3_graphs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object GraphsBasics extends App {

  implicit val system = ActorSystem("graphs")
  implicit val materializer = ActorMaterializer()



}

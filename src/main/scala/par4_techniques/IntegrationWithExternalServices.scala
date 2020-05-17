package par4_techniques

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object IntegrationWithExternalServices extends App {

  implicit val system = ActorSystem("externalServices")
  implicit val materializer = ActorMaterializer()

  implicit val dedicated = system.dispatchers.lookup("dedicated-dispacher")



}

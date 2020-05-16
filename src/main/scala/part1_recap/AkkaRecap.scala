package part1_recap

import akka.actor.{Actor, ActorSystem, Props, Stash}
import akka.pattern.AskableActorRef
import akka.util.Timeout

object AkkaRecap extends App {

  class SimpleActor extends Actor with Stash {
    override def receive: Receive = {
      case "createChild" =>
        val child = context.actorOf(Props[SimpleActor], "myChild")
        child ! "say hello child"
      case "stashThis" => stash()
      case "change" => context.become(anotherHandler)
      case "question" => sender() ! "hummm, this is a response from another actor.."
      case message => println(s"i received $message")
    }

    def anotherHandler: Receive = {
      case message => println(s"in another receive ${message}")
    }

  }

  //actor encapsulation
  val system = ActorSystem("AkkaRecap")

  val actor = system.actorOf(Props[SimpleActor], "simpleActor")
  val anotherActor = system.actorOf(Props[SimpleActor], "anotherSimpleActor")

  actor ! "Hiiiii"

  import system.dispatcher
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.pattern._

  implicit val timeout: Timeout = Timeout(3 seconds)

  val future = actor ? "question"

  future.mapTo[String].pipeTo(anotherActor)

}

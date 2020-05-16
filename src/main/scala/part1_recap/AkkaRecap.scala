package part1_recap

import akka.actor.{Actor, ActorSystem, Props, Stash}

object AkkaRecap extends App {

  class SimpleActor extends Actor with Stash{
    override def receive: Receive = {
      case "createChild" =>
        val child = context.actorOf(Props[SimpleActor], "myChild")
        child ! "say hello child"
      case "stashThis" => stash()
      case message => println(s"i received $message")
      case "change" => context.become(anotherHandler)
    }

    def anotherHandler: Receive = {
      case message => println(s"in another receive ${message}")
    }

  }

  //actor encapsulation
  val system = ActorSystem("AkkaRecap")

  val actor = system.actorOf(Props[SimpleActor], "simpleActor")

  actor ! "Hiiiii"


}

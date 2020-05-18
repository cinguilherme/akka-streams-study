package part5_advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches}

object DinamicStreamHandling extends App {

  import scala.concurrent.duration._
  implicit val system = ActorSystem("dinamic")
  implicit val materializer = ActorMaterializer()

  // KILL switch
  val killSwitchFlow = KillSwitches.single[Int]

  val ss = Source(Stream.from(1)).throttle(1, 1 second).log("counter")
  val sink = Sink.ignore

  val killS = ss.viaMat(killSwitchFlow)(Keep.right)
    .to(sink).run()

  import system.dispatcher
  system.scheduler.scheduleOnce(3 seconds){
    killS.shutdown()
  }

  val sharedKillSwitch = KillSwitches.shared("oneForAll")

  val s1 = Source(Stream.from(1)).throttle(1, 1 second).log("s1")
  val s2 = Source(Stream.from(1)).throttle(3, 1 second).log("s2")

  s1.via(sharedKillSwitch.flow).to(Sink.ignore).run()
  s2.via(sharedKillSwitch.flow).to(Sink.ignore).run()

  import system.dispatcher
  system.scheduler.scheduleOnce(3 seconds){
    sharedKillSwitch.shutdown()
  }

}

package part5_advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches}

object DinamicStreamHandling extends App {

  import scala.concurrent.duration._
  implicit val system = ActorSystem("dinamic")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  // KILL switch
  val killSwitchFlow = KillSwitches.single[Int]

//  val ss = Source(Stream.from(1)).throttle(1, 1 second).log("counter")
  val sink = Sink.ignore

//  val killS = ss.viaMat(killSwitchFlow)(Keep.right)
//    .to(sink).run()

  import system.dispatcher
//  system.scheduler.scheduleOnce(3 seconds){
//    killS.shutdown()
//  }

//  val sharedKillSwitch = KillSwitches.shared("oneForAll")

//  val s1 = Source(Stream.from(1)).throttle(1, 1 second).log("s1")
//  val s2 = Source(Stream.from(1)).throttle(3, 1 second).log("s2")

//  s1.via(sharedKillSwitch.flow).to(Sink.ignore).run()
//  s2.via(sharedKillSwitch.flow).to(Sink.ignore).run()

//  system.scheduler.scheduleOnce(3 seconds){
//    sharedKillSwitch.shutdown()
//  }

  val simpleSource = Source(1 to 10)

  val dinamicMerge = MergeHub.source[Int]

  //we can use this sink anytime, this is not closed
  val materializedSink = dinamicMerge.to(Sink.foreach[Int](n => println(s"i got this $n"))).run()

  //Source(1 to 10).runWith(materializedSink)

  //broadcast
  val dinamicBroadcast = BroadcastHub.sink[Int]
  // thi scan be reused
//  val materializeSource = Source(1 to 20).log("dontMissMe").runWith(dinamicBroadcast)

  /**
    * combine a mergHUb and a broadcastHub
    *
    * publisher subscriber
    */

  val merge = MergeHub.source[String]
  val bcast = BroadcastHub.sink[String]

  val (publisherPort, subscriberPort) = merge.toMat(bcast)(Keep.both).run()

  subscriberPort.runWith(Sink.foreach[String](e => println(s"I received $e")))
  subscriberPort.map(e => e.length).runWith(Sink.foreach[Int](s => println(s"I have received a string with size of $s")))
  Source.single("Striiiiings").runWith(publisherPort)
  Source(List("Akka", "is", "awesome")).runWith(publisherPort)
}

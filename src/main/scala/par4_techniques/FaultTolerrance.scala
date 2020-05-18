package par4_techniques

import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{RestartSource, Sink, Source}

import scala.util.Random

object FaultTolerrance extends App {

  implicit val system = ActorSystem("faultTolerrance")

  implicit val materializer = ActorMaterializer()
  import scala.concurrent.duration._
  // 1 Logging

  val faltySource = Source(1 to 10).map(e => if (e == 6) throw new RuntimeException else e)
  //faltySource.log("trackingElements").to(Sink.ignore).run()

  //gracefully termination stream 2
//  faltySource.recover {
//    case _: RuntimeException => Int.MinValue
//  }.log("gracefullSource").to(Sink.ignore).run()

  // recover with another stream 3
//  faltySource.recoverWithRetries(3,{
//    case _: RuntimeException => Source(9 to 99)
//  }).log("recoverWithReties").to(Sink.ignore).run()


  // backoff supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 1 second,
    maxBackoff = 10 second,
    randomFactor = 0.2
  )(() => {
    val randomNumber = new Random().nextInt(20)
    Source(1 to 20).map(ele => if (ele == randomNumber) throw new RuntimeException else ele)
  })

  //restartSource.log("restartBackoff").to(Sink.ignore).run()

  // supervision strategy
  val numbers = Source(1 to 20).map(x => if(x == 13) throw new RuntimeException else x).log("supervision")
  val superVisedNumbers = numbers.withAttributes(ActorAttributes.supervisionStrategy {
    case _: RuntimeException => Resume
    case _ => Stop
  })

  superVisedNumbers.to(Sink.ignore).run()


}

package par4_techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object AdvancedBackPressure extends App {

  implicit val system = ActorSystem("backpressure")
  implicit val materializer = ActorMaterializer()

  val flow = Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.backpressure)

  case class PagerEvent(descrition: String, date: Date,  nInstances: Int = 1)
  case class Notification(email: String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("serviceDiscoveryFailed", new Date),
    PagerEvent("illegalElements", new Date),
    PagerEvent("numberOfHttp500Spiked", new Date),
    PagerEvent("serviceStopedResponding", new Date)
  )

  val eventSurce = Source(events)

  val onCallEng = "daniel@rtjvm.com"

  def sendEmail(notification: Notification): Unit = {
    println(s"dear ${notification.email} you have an event: ${notification.pagerEvent}")
  }

  val notificationSink = Flow[PagerEvent]
    .map(event => Notification(onCallEng, event))
    .to(Sink.foreach(sendEmail))

  //eventSurce.to(notificationSink).run()

  /**
    * Un-backpressurable Source
    * Option 1, have a conflate flow
    * @param notification
    */

  def emailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s"dear ${notification.email} you have events: " +
      s"${notification.pagerEvent.nInstances}: " +
      s"${notification.pagerEvent.descrition}")
  }

  val aggFlow = Flow[PagerEvent]
    .conflate((event1, event2) => {
      val nInstances = event1.nInstances + event2.nInstances
      val combinedText = event1.descrition + ", "+event2.descrition
      PagerEvent(combinedText, new Date, nInstances)
    }).map(pager => Notification(onCallEng, pager))

//  eventSurce.via(aggFlow).async
//    .to(Sink.foreach[Notification](emailSlow)).run()

  import scala.concurrent.duration._
  val slowSource = Source(Stream.from(1)).throttle(1, 1 second)
  val hungrySink = Sink.foreach[Int](println)

  val extrapollator = Flow[Int].extrapolate(element => Iterator.from(element))
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))
  val expander = Flow[Int].expand(element => Iterator.from(element))

  //slowSource.via(repeater).to(hungrySink).run()

}

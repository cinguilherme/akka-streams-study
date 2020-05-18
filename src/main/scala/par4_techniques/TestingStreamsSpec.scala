package par4_techniques

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TestingStreamsSpec extends TestKit(ActorSystem("testingAkkaStreams"))
  with WordSpecLike
  with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "simple stream" should {
    "satisfy basic assertions" in {
      // describe the test
      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold[Int, Int](0)((a,b) => a+b)

      val f = simpleSource.toMat(simpleSink)(Keep.right).run()
      val sum = Await.result(f, 1 second)
      assert(sum == 55)
    }

    "integrate with test actors" in {
      import akka.pattern.pipe
      import system.dispatcher
      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold[Int, Int](0)((a,b) => a+b)
      val probe = TestProbe()
      val f = simpleSource.toMat(simpleSink)(Keep.right).run().pipeTo(probe.ref)
      probe.expectMsg(55)
    }

    "integrate with a test-actor-based sink" in {
      val simpleSource = Source(1 to 5)
      val flow = Flow[Int].scan(0)(_ + _)
      val streamUnderTest = simpleSource.via(flow)

      val testProbe = TestProbe()
      val probeSink = Sink.actorRef(testProbe.ref, "completionMessage")

      streamUnderTest.to(probeSink).run()

      testProbe.expectMsgAllOf(0, 1, 3, 6, 10, 15)
    }

    "integrate with streams testkit Sink" in {
      val sourceUnderTest = Source(1 to 5).map(_ * 2)

      val testSink = TestSink.probe[Int]

      val res = sourceUnderTest.runWith(testSink)

      res.request(5)
        .expectNext(2,4,6,8,10)
        .expectComplete()
    }

    "integrate with streams testkit source" in {
      import system.dispatcher
      val sinkUnderTest = Sink.foreach[Int] {
        case 13 => throw new RuntimeException
        case _ =>
      }

      val testSource = TestSource.probe[Int]
      val materialized = testSource.toMat(sinkUnderTest)(Keep.both).run()
      val (testPub, resultFut) = materialized

      testPub
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      resultFut.onComplete {
        case Success(_) => fail()
        case Failure(_) =>
      }
    }

    "test flows with a testSink and testSource" in {
      val flowUnderTest = Flow[Int].map(_ * 2)

      val testSource = TestSource.probe[Int]
      val testSink = TestSink.probe[Int]

      val matt = testSource.via(flowUnderTest).toMat(testSink)(Keep.both).run()
      val (publisher, subscriber) = matt

      publisher
        .sendNext(1)
        .sendNext(2)
        .sendNext(5)
        .sendNext(42)
        .sendNext(99)
          .sendComplete()

      subscriber.request(5)
      subscriber.expectNext(2)
        .expectNext(4)
        .expectNext(10)
        .expectNext(84)
        .expectNext(198)
        .expectComplete()
    }
  }

}

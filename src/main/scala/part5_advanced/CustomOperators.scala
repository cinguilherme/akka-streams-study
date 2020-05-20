package part5_advanced

import java.util.NoSuchElementException
import java.util.function.Predicate

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, Outlet, SinkShape, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.collection.mutable
import scala.util.Random

object CustomOperators extends App {

  implicit val system = ActorSystem("customOperators")
  implicit val materializer = ActorMaterializer()


  class RandomNumberGenerator(max: Int) extends GraphStage[SourceShape[Int]] {

    val outPort = Outlet[Int]("randomGenerator")
    val random = new Random()

    override def shape: SourceShape[Int] = SourceShape(outPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

      setHandler(outPort, new OutHandler {

        override def onPull(): Unit = {
          val value = random.nextInt(max)
          push(outPort, value)
        }
      })
    }
  }

  val randomNumberGeneratorSource = Source.fromGraph(new RandomNumberGenerator(100))
  //randomNumberGeneratorSource.runWith(Sink.foreach(println))

  //custom sink to print in batches
  class BatcherSink(batchSize: Int) extends GraphStage[SinkShape[Int]] {

    val inport = Inlet[Int]("batcher")

    override def shape: SinkShape[Int] = SinkShape[Int](inport)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

      override def preStart() {
        pull(inport)
      }

      //mutable state
      val batch = new mutable.Queue[Int]

      setHandler(inport, new InHandler {
        override def onPush(): Unit = {
          val nextEle = grab(inport)
          batch.enqueue(nextEle)

          // assume xomplex
          Thread.sleep(100)
          if(batch.size >= batchSize) {
            println("new batch: " + batch.dequeueAll(_ => true).mkString("[",", ","]"))
          }
          pull(inport) //send demand upstream
        }
      })
    }
  }
  val siker = Sink.fromGraph(new BatcherSink(10))

  //randomNumberGeneratorSource.to(siker).run()

  /**
    * Create a custom Filter Flow
    */
  class CustomFilterFlow[T](predicate: T => Boolean) extends GraphStage[FlowShape[T, T]] {

    val inport = Inlet[T]("inport")
    val outport = Outlet[T]("outport")

    override def shape: FlowShape[T, T] = FlowShape[T,T](inport, outport)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

      setHandler(inport, new InHandler {
        override def onPush(): Unit = {

          val v:T = grab(inport)

          val bool = predicate(v)
          if(bool){
            push(outport, v)
          } else {
            pull(inport)
          }
        }
      })

      setHandler(outport, new OutHandler {
        override def onPull(): Unit = pull(inport)
      })
    }
  }

  val ff = Flow.fromGraph(new CustomFilterFlow[Int]((x:Int) => x > 5))

  Source(Stream.from(1)).via(ff).to(Sink.foreach[Int](v => println(s"I am the final sink, i got this here $v"))).run()

}

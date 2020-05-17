package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Sink, Source, Zip, ZipWith}

object GraphCycles extends App {

  implicit val system = ActorSystem("graphCycles")
  implicit val materializer = ActorMaterializer()

  val accelerator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))

    val incrementer = builder.add(Flow[Int].map(_ + 1))

    sourceShape ~>  mergeShape ~> incrementer
                    mergeShape <~ incrementer
    ClosedShape
  }

  //Cycle DEAD LOCK, due to backpressure
  //RunnableGraph.fromGraph(accelerator).run()

  //Solution 1 - MergePreffered
  val acceleratorPreff = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(MergePreferred[Int](1))

    val incrementer = builder.add(Flow[Int].map(x => {println(s"incrementing $x") ;x + 1}))

    sourceShape ~>  mergeShape ~> incrementer
    mergeShape.preferred <~ incrementer
    ClosedShape
  }
//  RunnableGraph.fromGraph(acceleratorPreff).run()

  //Solution 2 - Buffers
  val bufferRepeater = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))

    val repeater = builder.add(Flow[Int]
      .buffer(10, OverflowStrategy.dropHead)
      .map(x => {
        println(s"repearter $x")
        x
      }))

    sourceShape ~>  mergeShape ~> repeater
    mergeShape <~ repeater
    ClosedShape
  }
//  RunnableGraph.fromGraph(bufferRepeater).run()

  /**
    * Graphs Cycle
    * big risk of DEAD locking
    * Unboundness in cycle - add bounds to number of elements
    * BOUNDNESS vs LIVENESS
    *
    * Challange - create a fanInShape takes 2 inputs and emits an infinite
    * fibonacci numbers starting from this 2 numbers
    *
    * Hint: Use ZipWith and Cycles
    */

  def infiniteFib() = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(Zip[BigInt, BigInt])
      val mergePref = builder.add(MergePreferred[(BigInt, BigInt)](1))

      val mapper = builder.add(Flow[(BigInt, BigInt)]
        .map(x => {
          Thread.sleep(100)
          (x._1 + x._2, x._1)
        }))

      val broadcast = builder.add(Broadcast[(BigInt, BigInt)](2))

      val fbPrinter = builder.add(Flow[(BigInt, BigInt)].map(x => x._1))

      zip.out ~>  mergePref ~> mapper ~> broadcast ~> fbPrinter
                  mergePref.preferred <~ broadcast


      UniformFanInShape(fbPrinter.out, zip.in0, zip.in1)
    }
  }

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val source = builder.add(Source.single[BigInt](1))
    val sourceTwo = builder.add(Source.single[BigInt](1))

    val sink = builder.add(Sink.foreach[BigInt](println))

    val figg = builder.add(infiniteFib())

    source    ~> figg.in(0)
    sourceTwo ~> figg.in(1)

    figg.out ~> sink

    ClosedShape
  }).run()

}

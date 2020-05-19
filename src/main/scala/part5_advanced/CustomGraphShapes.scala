package part5_advanced

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Balance, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape, Graph, Inlet, Outlet, Shape}

import scala.concurrent.duration._

object CustomGraphShapes extends App {

  implicit val system = ActorSystem("customGraphs")
  implicit val materializer = ActorMaterializer()

  case class BalanceNXM[T](override val inlets: List[Inlet[T]],
                           override val outlets: List[Outlet[T]]) extends Shape {

    override def deepCopy(): Shape = {
      BalanceNXM(inlets.map(_.carbonCopy()), outlets.map(_.carbonCopy()))
    }
  }

  object BalanceNXM {
    def apply[T](inputs: Int, output: Int): Graph[BalanceNXM[T], NotUsed] = {
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val merge = builder.add(Merge[T](inputs))
        val balance = builder.add(Balance[T](output))

        merge ~> balance

        BalanceNXM(merge.inlets.toList, balance.outlets.toList)
      }
    }
  }

  val runGr = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      def createSource(n: Int) = Source(Stream.from(1))
        .throttle(n, 1 second)


      val source1 = createSource(1)
      val source2 = createSource(2)
      val source3 = createSource(3)
      val source4 = createSource(4)

      val sink1 = builder.add(Sink.ignore)
      val sink2 = builder.add(Sink.foreach[Int](println))
      val sink3 = builder.add(Sink.fold(0)((c:Int , e: Int) => {
        println(s"sink 3 got element $e and its count is at $c")
        c + 1
      }))

      val balance4X8 = builder.add(BalanceNXM[Int](4,3))

      source1 ~> balance4X8.inlets(0)
      source2 ~> balance4X8.inlets(1)
      source3 ~> balance4X8.inlets(2)
      source4 ~> balance4X8.inlets(3)

      import GraphDSL.Implicits._

      balance4X8.outlets(0) ~> sink1
      balance4X8.outlets(1) ~> sink2
      balance4X8.outlets(2) ~> sink3

      ClosedShape
    }
  )

  runGr.run()
}

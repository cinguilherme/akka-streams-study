package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object FirstPrinciples extends App {

  implicit val system = ActorSystem("firstPrinciples")
  implicit val materializer = ActorMaterializer()

  //source, Upstream
  val source = Source(1 to 10)

  //sink
  val sink = Sink.foreach[Int](println)


  //definition of a akka stream
  val graph = source.to(sink)


  graph.run()


  //flows transforms elements

  val flow = Flow[Int].map(_ + 1)
  val sourceWithFlow = source.via(flow)

  /**
    * Exercice create a stream of person , take the first 2 names longer than 5 char.
    * print results in the end
    *
    */

    case class Person(name: String)

  val listPerson = List(Person("Ann"), Person("Tonny"), Person("August"), Person("Troy"),Person("Amanda"))
  val sourcePerson = Source(listPerson)
  val longNameFlow = Flow[String].filter(s => s.length > 5)
  val firtTwo = Flow[String].take(2)

  sourcePerson.map(p => p.name)
    .via(longNameFlow)
    .via(firtTwo)
    .to(Sink.foreach(println)).run()

  sourcePerson.filter(_.name.length > 5).take(2).map(_.name).runForeach(println)

}

package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object MaterializingStreams extends App {

  implicit val system = ActorSystem("materializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

//  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))

//  val simpleMaterializedValue = simpleGraph.run()

//  val sink = Sink.reduce[Int](_ + _)
//  val sumFuture = Source(1 to 100).runWith(sink)
//
//  sumFuture.onComplete {
//    case Success(value) => println(s"sum of all values is $value")
//    case Failure(_) => println("unnable to complete the operation")
//  }


  // choosing materialized values
  val simSource = Source(1 to 10)
  val simFlow = Flow[Int].map(_ + 1)
  val simSink = Sink.foreach[Int](println)
//  simSource.viaMat(simFlow)(Keep.right).toMat(simSink)(Keep.right).run().onComplete {
//    case Success(value) => println(s"wow coool $value")
//    case Failure(_) => println("failure epic")
//  }

  /**
    * Exercices
    * return the last element in a source
    * compute the total word count out o stream of sentences
    *
    */

  // last element
  val flowz = Flow[List[Int]].map(l => l.reverse)
  Source(1 to 10).runWith(Sink.reduce[Int]((f,s)=> s)).onComplete {
    case Success(value) => println(value)
    case _ => println("fonfon")
  }
  Source(1 to 10).runWith(Sink.takeLast(1)).onComplete {
    case Success(value) => println(value)
    case _ => println("fiufiu")
  }
  Source(1 to 10).runWith(Sink.last)

  // word counter
  val listSentences = List("this is one", "this is the second", "adding another sentence to this list")
  val wordFlow = Flow[String].map(s => s.split(" ").size)
  val sinkReducer = Sink.reduce[Int](_ + _)


  Source(listSentences).via(wordFlow).runWith(sinkReducer).onComplete{
    case Success(value) => println(s"success, total number of words $value")
    case _ => println("frafrafra")
  }
  Source(listSentences)
    .viaMat(wordFlow)(Keep.right).runWith(sinkReducer).onComplete {
    case Success(value) => println(s"success, total number of words $value")
    case _ => println("faillzz")
  }
}

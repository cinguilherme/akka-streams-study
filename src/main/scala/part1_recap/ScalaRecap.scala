package part1_recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {

  val aCondition: Boolean = false
  val name: String = "name"

  val valF = (x: Int) => {
    x + 1
  }

  def fun(x: Int) = {

    1
  }

  println(valF(2) + fun(5))

  class Animal

  trait Carnivore {

  }

  object Carnivore


  //genereics

  abstract class MyList[+A]


  //method notations
  1 + 2
  1.+(2)

  // FP

  val inc: Int => Int = (i: Int) => i + 1
  List(1,2,3).map(inc)
  // HOF
  // for-comprehensions

  //MONADS -> Option, Try

  // Pattern Matching
  val unkown: Any = 2

  val order = unkown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "not known"
  }

  try {
    throw new RuntimeException
  } catch {
    case e: Exception => println("exception")
  }

  // scala advanced

  // multithreading fundamentals

  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    //long compute
    42
  }

  future.onComplete {
    case Success(value) => println(s"the value is $value")
    case Failure(_) => println(s"I found an exception on calculation ")
    case _ => println("i dont know")
  }

  val pf: PartialFunction[Int, Int] = {
    case 1 => 1
    case 2 => 10
    case _ => 0
  }

  type AkkaReceive = PartialFunction[Any, Unit]
  def receive: AkkaReceive = {
    case 1 => println("hi you")
    case _ => println("i dont know")
  }

  receive(1)
  receive(2)

  // Implicits
  implicit val timeout = 3000
  def setTimeuot(f: () => Unit)(implicit timeout:Int) = f()

  setTimeuot(() => println("timeout"))

  case class Person(name: String) {
    def greet: String = s"Hi! my name is $name"
  }

  implicit def fromStringToPerson(name: String) = Person(name)

  println("Petter".greet)

  implicit class Dog(name: String){
    def bark = println("bark")
  }

  "Lassie".bark
}

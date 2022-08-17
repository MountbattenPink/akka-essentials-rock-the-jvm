package playground

object AdvancedRecap extends App {

  //partial function
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function : Int => Int = partialFunction

  val modifiedList = List(1,2,3).map{
    case 1 => 42
    case 2 => 65
    case 5 => 999
    case anythingElse => 1
  }

  val lifted = partialFunction.lift //Int => Option[Int]

  //adding more cases
  val pfChain = partialFunction.orElse[Int, Int]{
    case 100 => 11111
  }

  //implicits
  implicit val timeout = 3000
  def setTimeout(f:()=> Unit)(implicit t:Int): Unit =f()
  setTimeout(() => println("Implicit was injected"))


  case class Person(name:String){
    def greet = println(s"Hi, I'm $name")
  }
  implicit def fromStringToPerson(name: String) = Person(name)
  "Peter".greet

  //implicit class

  implicit class Dog(name: String){
def bark = println("bark")
  }
  "Lassie".bark

  //order of getting implicits:
  //1.local scope
  //2.imported scope
  //3.companion object scope

}

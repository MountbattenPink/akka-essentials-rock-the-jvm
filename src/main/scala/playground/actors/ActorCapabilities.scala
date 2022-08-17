package playground.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import playground.actors.ActorCapabilities.BankAccount.{Deposit, TransactionFailure, TransactionSuccess, Withdraw}
import playground.actors.ActorCapabilities.Domain.{Decrement, Increment, Print}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    // context.self.path
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello there!" //replying
      case message: String => println(s"simple actor ${self.path.name} - I received string $message")
      case number: Int => println(s"simple actor ${self.path.name} - I received number $number")
      case specialMessage: SpecialMessage => println(s"simple actor ${self.path.name} - I received special message ${specialMessage.content}")
      case messageToYourself: SendMessageToYourself => println(s"simple actor ${self.path.name} - recieved messageToYourself ${messageToYourself.content}")
        context.self ! messageToYourself.content
      case sayHi: SayHi => println(s"simple actor ${self.path.name} - I received say hi")
        (sayHi.actor ! s"Hi!") (self)
      case wirelessPhoneMessage: WirelessPhoneMessage =>
        println(s"simple actor ${self.path.name} - I received say wirelessPhoneMessage")
        wirelessPhoneMessage.actorRef forward wirelessPhoneMessage.content + "s" //I keep the original sender of message
    }
  }

  val actorSystem = ActorSystem("ActorCapabilities")
  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "Hello actor"

  //messages can be of any type but
  // message must be immutable
  // messages must be serializable
  //in practice, use case classes/case objects
  simpleActor ! 3

  case class SpecialMessage(content: String)

  simpleActor ! SpecialMessage("special content")

  //actors have information about context and itself


  case class SendMessageToYourself(content: String)

  simpleActor ! SendMessageToYourself("send to yourself")

  //actors can reply to messages

  val alice = actorSystem.actorOf(Props[SimpleActor], "Alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "Bob")

  case class SayHi(actor: ActorRef)

  alice ! SayHi(bob)

  //forwarding messages - sending message to an original sender
  case class WirelessPhoneMessage(content: String, actorRef: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob)


  //exercises
  /*
  1. counter (increment, decrement + print)
  2. bank account (deposit, withdraw and amount, statement)
  + reply success/failure to each operation
  + interract with other actor
   */


  class CountActor extends Actor {
    var count: Int = 0

    override def receive: Receive = {
      case Increment => println(s"${self.path.name}: Received increment. $count")
        count += 1
      case Decrement => println(s"${self.path.name}: Received decrement. $count")
        count -= 1
      case Print => println(s"${self.path.name}: Received println. $count")
    }
  }


  val countActorRef = actorSystem.actorOf(Props[CountActor], "countActor")
  (1 to 5).foreach { _ => countActorRef ! Increment }
  (1 to 3).foreach { _ => countActorRef ! Decrement }
  countActorRef ! Print

  object Domain {
    case object Increment

    case object Decrement

    case object Print
  }


  class BankAccount extends Actor {
    var funds: Int = 0

    override def receive: Receive = {
      case Deposit(amount) => funds += amount
        sender() ! TransactionSuccess("deposit success")
      case Withdraw(amount) =>
        if (funds < amount) sender() ! TransactionFailure("invalid amount")
        else if (amount <= 0) sender() ! TransactionFailure("invalid withdraw")
        else {
          funds -= amount
          sender() ! TransactionSuccess("successfully withdrawn")
        }
      case Print => println(funds)
        sender() ! funds.toString
    }
  }

  case class LifeALive(actor: ActorRef)

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case LifeALive(account: ActorRef) =>
        account ! Deposit(1000)
        account ! Withdraw(10000)
        account ! Withdraw(50)
        account ! Print

      case anythingElse => println(anythingElse.toString)
    }
  }

  object BankAccount {
    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object Print

    case class TransactionSuccess(msg: String)

    case class TransactionFailure(msg: String)
  }


  val account = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val person = actorSystem.actorOf(Props(new Person("name")), "billionaire")
  person ! LifeALive(account)
}

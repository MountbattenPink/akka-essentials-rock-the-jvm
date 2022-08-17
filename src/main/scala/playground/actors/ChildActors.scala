package playground.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import playground.actors.ActorCapabilities.BankAccount.{Deposit, Withdraw}
import playground.actors.ChildActors.CreditCard.{Attach, CheckStatus}
import playground.actors.ChildActors.NaiveBankAccount.Init
import playground.actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  object Parent {
    case class CreateChild(name: String)

    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child $name ")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))


    }

    def withChild(child: ActorRef): Receive = {
      case TellChild(message) =>
        if (child!=null) println(s"telling child ${child.path} $message")
        child ! message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} received message $message")
    }
  }


  val system = ActorSystem("actorSystem")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey kid")


  //actor hierarchy
  //parent -> child1 -> grandChild
  //       -> child2

  //Gurdian (top level) actors
  //1. /system - system level (logging etc.)
  //2. /user - user level
  //3. / - root guardian (manages /system and /user)

  //Actor selection

  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  //NEVER PASS MUTABLE ACTOR STATE OR CHILD REFERENCES!!! (DANGER OF ACTOR ENCAPSULATION)
//THIS IS CALLED CLOSING OVER

  object NaiveBankAccount{
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Init
  }
  class NaiveBankAccount extends Actor{
    var amount = 0


    override def receive: Receive = {
      case Init =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! Attach(this)
      case Deposit(funds) =>  depositFunds(funds)
      case Withdraw(funds) => withdrawFunds(funds)
    }

    def depositFunds(funds: Int) = {
      println(s"${self.path} depositing $funds")
    amount+=funds
    }

    def withdrawFunds(funds: Int): Unit = {
      println(s"${self.path} withdrawings $funds")

      amount-=funds
    }

  }

  object CreditCard{
    //need ActorRef!!!, not NaiveBankAccount
    case class Attach(bankAccount: NaiveBankAccount)
    case object CheckStatus
  }
  class CreditCard extends Actor{

    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s" ${self.path} your message has been processed")
        account.withdrawFunds(1) // because I can and this is the problem
    }

    override def receive: Receive = {
      case Attach(account) => context.become(attachedTo(account))
    }
  }

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "bank")
  bankAccountRef ! Init
  bankAccountRef ! Deposit(10000)
  Thread.sleep(1000)
  val creditCardSelection = system.actorSelection("/user/bank/card")
  creditCardSelection ! CheckStatus
}

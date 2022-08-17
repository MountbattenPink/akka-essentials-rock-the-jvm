package playground

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MultiThreadingRecap extends App {

  //creating a thread
  val thread = new Thread(() => println("run"))
  thread.start()
  thread.join()


  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodBye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  //threadHello.start()
  //threadGoodBye.start()

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = {
      amount -= money
    }

    def safeWithdraw(money: Int) = synchronized {
      amount -= money
    }

  }

  //futures
  val future = Future {
    42
  }
}
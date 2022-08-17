package playground

object ThreadModelLimitations extends App {
  //Daniels rants:
  /*
  1. OOP encapsulation is only valid in a single thread model
   */
  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = {
      amount -= money
    }

    def deposit(money: Int) = {
      amount += money
    }

    def getAmount = amount
  }

  val account = new BankAccount(2000)
/*  for (_ <- 1 to 1000) {
    new Thread(() => account.withdraw(1)).start()
  }
  for (_ <- 1 to 1000) {
    new Thread(() => account.deposit(1)).start()
  }*/
  println(s"Amount ${account.getAmount}")

  /*
  2. delegating smth to a thread is a pain.
  you have a runnning thread and you want to pass a runnable to this thread
   */
var task: Runnable = null
  val runningThread : Thread = new Thread(() => {
    while(true){
      runningThread.synchronized{  while(task==null)println("waiting for a task")
        runningThread.wait()
      }
    }
  })

  task.synchronized{println("I have a task")
  task.run()
    task = null
  }

  def delegateBackGroundThread(r: Runnable)={
    if (task==null)task=r
    runningThread.synchronized(runningThread.notify())
  }

  runningThread.start()
  Thread.sleep(500)
  delegateBackGroundThread(() => println("42"))

  Thread.sleep(500)
  delegateBackGroundThread(() => println("this should run in background"))



  /*
  3. Tracing and error handling is a pain
   */
}

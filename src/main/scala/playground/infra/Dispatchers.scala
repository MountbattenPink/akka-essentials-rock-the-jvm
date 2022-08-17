package playground.infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.concurrent.Future
import scala.util.Random

object Dispatchers extends App {
  val as = ActorSystem("DispatchersDemo")//, ConfigFactory.load().getConfig("configDispatchersDemo"))
  class Counter extends Actor with ActorLogging{
    var count = 0

    override def receive: Receive = {
      case message => count+=1
        log.info(count+ " "+message.toString)
    }
  }

  //programatic
  val actors  = for (i <- 1 to 10) yield as.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  var r = new Random()
  for (i <- 1 to 1000){
    actors(r.nextInt(10)) ! i
  }
  Thread.sleep(3000)

  //config
 val rtjvmActor = as.actorOf(Props[Counter], s"counter_22")


  //dispatchers implement execution context trait
  class DBActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>  Future{
        Thread.sleep(5000)
        log.info(s"success: $message")
    }(context.system.dispatchers.lookup("my-dispatcher")) //(context.dispatcher)
       }
  }

  val dbActor = as.actorOf(Props[DBActor], s"DBActor")
dbActor ! "The meaning of life is 42"

  val nonBlockingActor =  as.actorOf(Props[Counter], s"c")
  for (i <- 1 to 1000){
    val message = s"important message $i"
    dbActor ! message
    nonBlockingActor ! message
  }

}

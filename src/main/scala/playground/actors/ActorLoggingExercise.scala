package playground.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingExercise extends App {

  class SimpleActorWithExplicitLogger extends Actor{
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }


  val as = ActorSystem("loggingDemo")
  val actor = as.actorOf(Props[SimpleActorWithExplicitLogger], "actorName")
  actor ! "message"


  //actorLogging

  class ActorWithLogging extends Actor with ActorLogging{
    override def receive: Receive = {
      case (a,b) => log.info("Two things: {} and {}", a,b)
        case message => log.info(message.toString)
      }
  }
  val actorWithLogger = as.actorOf(Props[ActorWithLogging], "actorWithBuiltInLogger")
  actorWithLogger ! "Logging a simple message by extending a trait"
  actorWithLogger ! ("Logging a simple message by extending a trait", "Logging another simple message by extending a trait")


}

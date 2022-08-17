package playground.faultTolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}

object ActorLifecycle extends App {
  val system = ActorSystem("as")

  case object StartChild

  class LifecycleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }

    override def preStart(): Unit = {
      log.info("Im starting")
    }

    override def postStop(): Unit = log.info("Im stopping")
  }

  val parent = system.actorOf(Props[LifecycleActor], "parent")
  parent ! StartChild
  parent ! PoisonPill

  object Fail
  object FailChild
  object CheckChild
  object Check


  class Parent extends Actor with ActorLogging {
    val child = context.actorOf(Props[Child], "supervisedchild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case Fail => child ! FailChild
    }
  }

  class Child extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("supervised child started")
    override def postStop(): Unit = log.info("supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"supervised actor restarting because of ${reason.getMessage} $message")
    }

    override def postRestart(reason: Throwable): Unit = {
      log.info(s"supervised actor restarted")
    }
    override def receive: Receive = {
      case Fail => log.warning("child will fail")
        throw new RuntimeException("I failed")
      case Check => log.info("alivee and kicking")
    }
  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild

}

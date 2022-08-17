package playground.faultTolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, StopChild, Terminated}
import playground.faultTolerance.StoppingActors.Parent.StartChild

object StoppingActors extends App {
  val system = ActorSystem("as")

  object Parent{
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }
  class Parent extends Actor with ActorLogging {
    override def receive: Receive = withCHildren(Map())

    def withCHildren(children: Map[String, ActorRef]): Receive = {
      case Parent.StartChild(name: String) =>
        log.info(s"Starting child $name")
        context.become(withCHildren(children + (name -> context.actorOf(Props[Child], name))))
      case Parent.StopChild(name: String) =>
        log.info(s"sopping child with the name $name")
        children.get(name).map(child => context.stop(child))
      case Parent.Stop => context.stop(self)
      case message => log.info(s"received parent $message")
    }
  }

  class Child extends Actor  with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // context.stop()
  val parent = system.actorOf(Props[Parent],"parent")
  parent ! StartChild("child1")
  val child = system.actorSelection("/user/parent/child1")
  child ! "hi kid"
  parent ! Parent.StopChild("child1")
//  for (_ <- 1 to 50) child ! "are you still there?"
  parent ! Parent.StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "hi second child"
  parent ! Parent.Stop
 // for (_ <-1 to 10) parent ! "parent, are you still there"
 // for (i <-1 to 100) child2 ! s"$i child2, are you still there"

  //special messages
  val looseActor = system.actorOf(Props[Child])
  looseActor ! "hello loose actor"
  looseActor ! PoisonPill
  looseActor ! "loose actor are you still there?"

  val abruptlyTerminatedActor = system.actorOf(Props[Child])
  abruptlyTerminatedActor ! "you are about to be terminated"
  abruptlyTerminatedActor ! Kill
  abruptlyTerminatedActor ! "you have been terminated"

  // death watch
  class Watcher extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = {
      case Parent.StartChild(name: String) =>
        log.info(s"Starting child $name")
        val child =  context.actorOf(Props[Child], name)
        context.watch(child)
case Terminated(ref) => log.info(s"the reference I'm watching $ref, has been stopped")
    }
  }
  val watcher =  system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(1000)
  watchedChild ! PoisonPill


}

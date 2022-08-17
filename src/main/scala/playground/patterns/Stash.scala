package playground.patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object Stash extends App {
  /*
  ResourceActor
  ~open - can received read/write requests
  ~ otherwise - postpone until the state is open
  initially ResourceActor is closed

   */

  case object Open

  case object Close

  case object Read

  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        unstashAll()
        context.become(open)
      case message =>
        log.info("stashing")
        stash()
    }

    def open: Receive = {
      case Close =>
        log.info("closing")
        context.become(closed)
      case Read => log.info("read")
      case Write(data) => log.info(s"writing $data")
        innerData = data
      case message =>
        log.info("stashing")
        stash()
    }
  }

  val as = ActorSystem("StashDemo")
  val resourceActor = as.actorOf(Props[ResourceActor])
  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Write("I love stash")
  resourceActor ! Close
  resourceActor ! Read
}

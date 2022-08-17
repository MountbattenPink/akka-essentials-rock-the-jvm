package playground.infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration.DurationInt

object Schedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("simpleDEmo")
  val simpleActor = system.actorOf(Props[SimpleActor], "sa1")
  implicit val executionContext = system.dispatcher
  system.log.info("Scheduling actor")
  system.scheduler.scheduleOnce(1.second) {
    simpleActor ! "reminder"

    val routine: Cancellable = system.scheduler.schedule(1.second, 2.seconds) {
      simpleActor ! "heart beat"
    }
    system.scheduler.scheduleOnce(5.seconds) {
      routine.cancel()
    }
  }
  //self-closing actor

  class SelfClosingActor extends Actor with ActorLogging {
    var schedule = setTimeOutWIndow()

    override def receive: Receive = {
      case "timeout" => log.info("stopping myself")
        context.stop(self)
      case message: String => log.info("keeping alive")
        schedule.cancel()
        schedule = setTimeOutWIndow()
    }

    def setTimeOutWIndow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1.second) {
        self ! "timeout"
      }
    }
  }

  val selfCLosingActor = system.actorOf(Props[SelfClosingActor], "sa2")
  system.scheduler.scheduleOnce(250.millis) {
    selfCLosingActor ! "ping"
  }
  system.scheduler.scheduleOnce(2.seconds) {
    system.log.info("sending pong to self closing actor")
    selfCLosingActor ! "pong"
  }

  //timers

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TImeBasedHeartBeatActor  extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500.millis)
    override def receive: Receive = {
      case Start => log.info("bootstraping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1000.millis)
      case Reminder => log.info("I'm alive")
      case Stop => log.warning("stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }
  val heartBeatActor = system.actorOf(Props[TImeBasedHeartBeatActor], "TImeBasedHeartBeatActor")
system.scheduler.scheduleOnce(5.seconds){
  heartBeatActor ! Stop
}
}

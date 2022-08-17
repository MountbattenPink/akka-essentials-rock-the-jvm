package playground.infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

object Routers extends App {
  //1 manually create router

  class Master extends Actor with ActorLogging {
    //step 1 - create routees

    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave-$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }
    //step 2 - define router
    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      case Terminated(ref) => router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        router.addRoutee(newSlave)
      case message => router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val as = ActorSystem("simpleDEmo", ConfigFactory.load().getConfig("routerDemo"))
  val master = as.actorOf(Props[Master], "master")
  for (i <- 1 to 10) master ! s"[$i] Hello from the world"
  Thread.sleep(3000)
  //programatic
  val poolMaster = as.actorOf(RoundRobinPool(5).props(Props[Slave]), "masterpool")
  for (i <- 1 to 10) poolMaster ! s"[$i] Hello from the world"

  Thread.sleep(3000)

  //from config
  val poolMaster2 = as.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")

  for (i <- 1 to 10) poolMaster2 ! s"[$i] Hello from the world"
  Thread.sleep(3000)


  //router with actors created elswhere (group router)
  //in another part of my application
  val slaveList = (1 to 5).map(i => as.actorOf(Props[Slave], s"slave_$i")).toList
  val slavePath = slaveList.map(_.path.toString)

  val groupMaster = as.actorOf(RoundRobinGroup(slavePath).props())

  for (i <- 1 to 10) groupMaster ! s"[$i] Hello from the world"
  Thread.sleep(3000)
val groupMaster2 = as.actorOf(FromConfig.props(), "groupMaster2")
  for (i <- 1 to 10) groupMaster2 ! s"[$i] Hello from the world"
  Thread.sleep(3000)


  //handling of special messages
  groupMaster2 ! Broadcast("hello everyone")
  Thread.sleep(3000)

  //PoinsonPill and Kill are NOT routed
  //Add/Remove/Get Routee are handled only by router
}

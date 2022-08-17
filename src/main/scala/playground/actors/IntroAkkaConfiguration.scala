package playground.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfiguration extends App {
  //1 inline
  val configString =
    """
      |akka{
      |   loglevel = "ERROR"
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val as = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))

  class LoggingActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val actor = as.actorOf(Props[LoggingActor], "actor1")
actor ! "message"


 val as2 = ActorSystem("ConfigurationFileDemo")
  val defaultConfigActor = as2.actorOf(Props[LoggingActor], "actor1")
  defaultConfigActor ! "some message"


  //separate config in the same file
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val as3 = ActorSystem("SpecialConfigurationFileDemo", specialConfig)
  val specialConfigActor = as3.actorOf(Props[LoggingActor], "actor")
  specialConfigActor ! "im special"

  //separate config file
  val separateConfig = ConfigFactory.load("application1.conf")
  println(separateConfig.getString("akka.loglevel"))

  //formats: JSON/PROPERTIES
  val jsonConfig = ConfigFactory.load("application2.json")
  println(jsonConfig.getString("akka.loglevel"))

  val propsConfig = ConfigFactory.load("application3.properties")
  println(propsConfig.getString("akka.loglevel"))

}

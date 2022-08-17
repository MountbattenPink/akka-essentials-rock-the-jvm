package playground.actors

import akka.actor.{Actor, ActorSystem, Props}

object Actors extends App {
  //actors are objects with which you cannot communicate directly, but can send async messages
  //all interaction is through async messages sending and receiving

  //part1 - actor system
  //it's better to have one unless more is needed
  val actorSystem = ActorSystem("FirstActorSystem")

  //part 2 - create actors
  //actors are uniquly identified
  //actors are async
  //actors have unique behavior
  //you cannot force actors


  //word count actor

  class WordCountActor extends Actor {
    var count = 0;

    override def receive: Receive = {
      case message: String =>
        println(s"received message $message")
        count += message.split(" ").length
      case anythingElse => println(s"I cannot understand $anythingElse")
    }
  }

  //instantiate actor

  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")


  //communicating with actor
  wordCounter ! "I'm learning Akka and it's pretty damn cool" //tell
  anotherWordCounter ! "One more message"

  object Person{
    def props(name: String) = Props(new Person(name))
  }
  class Person(name:String) extends Actor{
    override def receive: Receive = {
      case message => println(s"$name received message $message")
    }
  }

  val bob = actorSystem.actorOf(Person.props("Bob"))

  bob ! "hi"
}

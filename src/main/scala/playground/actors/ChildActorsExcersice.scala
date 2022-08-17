package playground.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExcersice extends App {

  //Distributed word count

  object WordCounterMaster{
    case class Init(nChildren:Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(taskId: Int, number:Int)
  }
  class WordCounterMaster extends Actor{
import WordCounterMaster._

    def receiveWithChildren(chidrenRefs: IndexedSeq[ActorRef], currentChildInd: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] received $text")
        val originalSender = sender()
        val childRef = chidrenRefs(currentChildInd)
        childRef ! WordCountTask(currentTaskId, text)
        val newTaskId = currentTaskId+1
        val newRequestMap = requestMap+(currentTaskId -> originalSender)
        context.become(receiveWithChildren(chidrenRefs, ((currentChildInd+1)%chidrenRefs.length), newTaskId, newRequestMap))

      case WordCountReply(id, n) =>
        println(s"[master] received $n from ${sender().path}")
        val originalSender = requestMap(id)
        originalSender ! n
        context.become(receiveWithChildren(chidrenRefs, currentChildInd, currentTaskId, requestMap-id))
    }

    override def receive: Receive = {
      case Init(n) =>
        println("Master initializing")
        val chidrenRefs: IndexedSeq[ActorRef] = (1 to n).map{i => context.actorOf(Props[WordCounterWorker],s"worker-$i")
      }
        context.become(receiveWithChildren(chidrenRefs,0,0, Map[Int,ActorRef]()))
    }
  }

  object WordCounterWorker{

  }
  class WordCounterWorker extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(taskId, text) =>
        println(s"worked received $text")
        sender() ! WordCountReply(taskId, text.split(" ").length)
    }
  }

  /**
   * create master,
   * send Init(10) - 10 workers
   * round robin logic
   */
class TestActor extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "MASTER")
        master ! Init(3)
        val texts = List("I LOVE AKKA", "SCALA IS SUPER DOPE", "YES, I ALSO THINK THE SAME")
        texts.foreach(text => master ! text)
      case count: Int => println(s"[test actor]: I received reply $count")
    }
  }
  val as = ActorSystem("as")
  val testActor = as.actorOf(Props[TestActor],"test")
  testActor ! "go"

}

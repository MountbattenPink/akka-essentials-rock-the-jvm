package playground.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import playground.actors.ChangingActorBehavior.Domain.{Decrement, Increment, Print}
import playground.actors.ChangingActorBehavior.FussyKid.{Accept, Happy, Reject, Sad}
import playground.actors.ChangingActorBehavior.Mom.{Ask, Choco, Food, Veggie}
import playground.actors.ChangingActorBehavior.{AggregateVotes, VoteStatusReply}

import scala.collection.mutable

object ChangingActorBehavior extends App {
val actorSystem = ActorSystem("changingActorBehaviorSystem")

  class FussyKid extends Actor {
    var state = Happy
    override def receive: Receive = {
      case Food(Veggie) => state = Sad
      case Food(Choco) => state = Happy
      case Ask(question) => if (state==Happy) sender() ! Accept
        if (state==Sad) sender() ! Reject
    }
  }

  class StatelessFussyKid extends Actor{
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(Veggie) => context.become(sadReceive, false)
      case Food(Choco) =>
      case Ask(_) => sender() ! Accept
    }
    def sadReceive: Receive = {
      case Food(Veggie) => context.become(sadReceive, false)
      case Food(Choco) => context.unbecome()
      case Ask(_) => sender() ! Reject
    }
  }

  object FussyKid{
    case object Accept
    case object Reject
    val Happy = "HAPPY"
    val Sad = "SAD"
  }

  object Mom{
    case class Food(food: String)
    case class Ask(ask: String)
val Veggie = "VEGETABLES"
val Choco = "CHOCOLATE"

  }
  class Mom extends Actor {
    override def receive: Receive = {
      case MomStart(kid) =>
        kid ! Food(Veggie)
        kid ! Ask("do you wanna play?")
        kid ! Food(Veggie)
        kid ! Ask("do you wanna play?")
        kid ! Food(Choco)
        kid ! Ask("do you wanna play?")
      case Accept => println("ACCEPT")
      case Reject => println("REJECT")
    }
  }
  case class MomStart(kid: ActorRef)

  val kid = actorSystem.actorOf(Props[FussyKid], "kid")
  val statelessKid = actorSystem.actorOf(Props[StatelessFussyKid], "statelesskid")
  val mom = actorSystem.actorOf(Props[Mom], "mom")
 // mom ! MomStart(kid)
  mom ! MomStart(statelessKid)


  /*
  excersice:
  1. recreate a counter actor with no mutable state
  2. simplifyed voting system
   */


  class CountActor extends Actor {
    var count: Int = 0

    def countReceive(i: Int): Receive = {
      case Increment => context.become(countReceive(i+1))
      case Decrement => context.become(countReceive(i-1))
      case Print => println(i)
    }

    override def receive: Receive = countReceive(0)
  }


  val countActorRef = actorSystem.actorOf(Props[CountActor], "countActor")
  (1 to 5).foreach { _ => countActorRef ! Increment }
  (1 to 3).foreach { _ => countActorRef ! Decrement }
  countActorRef ! Print

  object Domain {
    case object Increment

    case object Decrement

    case object Print
  }



  class Citizen extends Actor{
    override def receive: Receive = {
      case Vote(c) => context.become(voted(Some(c)))// candidate = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }
    def voted(candidate: Option[String]): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  class VoteAggregator extends Actor{

    override def receive: Receive = awaitingCommand

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate,0)
        val newStats = currentStats+(candidate -> (currentVotesOfCandidate+1))
        if (stillWaiting.isEmpty)println(s"Voting finished, current stats: $currentStats")
        else context.become(awaitingStatuses(newStillWaiting,newStats))
    }

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) => citizens.foreach {
        citizen => citizen ! VoteStatusRequest
      }
        context.become(awaitingStatuses(citizens, Map()))
    }
  }

  case class Vote(candidate:String)
  case class AggregateVotes(citizens: Set[ActorRef])
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  val alice = actorSystem.actorOf(Props[Citizen], "alice")
  val bob = actorSystem.actorOf(Props[Citizen], "bob")
  val charlie = actorSystem.actorOf(Props[Citizen], "charlie")
  val daniel = actorSystem.actorOf(Props[Citizen], "daniel")

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator], "va")
  voteAggregator ! AggregateVotes(Set(alice,bob,charlie,daniel))

  /**
   * Print status
   * like : Martin - 1, Jonas - 1, Roland - 2
   */



}

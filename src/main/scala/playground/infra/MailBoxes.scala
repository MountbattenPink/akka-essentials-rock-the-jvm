package playground.infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object MailBoxes extends App {
  val as = ActorSystem("as", ConfigFactory.load().getConfig("mailboxesdemo"))

  class LoggerActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  //interesting case 1: custom priority mailbox
  //step 1
  class SupportTicketPriorityMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(PriorityGenerator {
    case message: String if message.startsWith("[P0]") => 0
    case message: String if message.startsWith("[P1]") => 1
    case message: String if message.startsWith("[P2]") => 2
    case message: String if message.startsWith("[P3]") => 3
    case message => 4
  })

  //step 2 - in config
  // step 3
  val supportTicketActor = as.actorOf(Props[LoggerActor].withDispatcher("supportTicketDispatcher"))

  supportTicketActor ! PoisonPill
  Thread.sleep(1000)
  supportTicketActor ! "[P3]....."
  supportTicketActor ! "[P0]....."
  supportTicketActor ! "[P1]....."

  //control-aware mailbox
  //we'll use unbounded control aware mailbox
  //step 1 - mark important messages as control messages
  case object ManagementTicket extends ControlMessage
  //step 2 - configure who gets the mailbox

  val controlAwareActor = as.actorOf(Props[LoggerActor].withMailbox("control-mail-box"))
  controlAwareActor ! "[P0]....."
  controlAwareActor ! "[P1]....."
  controlAwareActor ! ManagementTicket


  //method 2 - using deployment config
  val altControlAwareActor=as.actorOf(Props[LoggerActor], "altControlAwareActor")

  altControlAwareActor ! "[P0]....."
  altControlAwareActor ! "[P1]....."
  altControlAwareActor ! ManagementTicket

}

package playground.patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.pattern.pipe
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.util.Success


class AskSpec extends TestKit(ActorSystem("askspec")) with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import AskSpec._
"Auth " should{
  "fail to auth a non-registered user" in {
    val authManaget = system.actorOf(Props[AuthManager])
    authManaget ! Authenticate("a","b")
    expectMsg(AuthFailure("not found"))
  }

  "fail to auth with incorrect pass" in {
    val authManaget = system.actorOf(Props[AuthManager])
    authManaget ! REgisterUser("a","b")
    authManaget ! Authenticate("a", "c")
    expectMsg(AuthFailure("incorrect"))
  }
}
  "piped" should{
    "pipeed fail to auth a non-registered user" in {
      val authManaget = system.actorOf(Props[PipedAuthManager])
      authManaget ! Authenticate("a","b")
      expectMsg(AuthFailure("not found"))
    }

    "piped fail to auth with incorrect pass" in {
      val authManaget = system.actorOf(Props[PipedAuthManager])
      authManaget ! REgisterUser("a","b")
      authManaget ! Authenticate("a", "c")
      expectMsg(AuthFailure("incorrect"))
    }
  }
}

object AskSpec {
  case class Read(key: String)

  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) => log.info(s"TRying to read the value at the key $key")
        sender() ! kv.get(key)
      case Write(k, v) => log.info(s"writing value $v key $kv")
        context.become(online(kv + (k -> v)))
    }
  }

  case class REgisterUser(name: String, pass: String)

  case class Authenticate(name: String, pass: String)

  case class AuthFailure(message: String)

  case object AuthSuccess

  class AuthManager extends Actor with ActorLogging {
    implicit val timeout: Timeout = Timeout(1.second)
    implicit val executionContext = context.dispatcher

    val authDB = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case REgisterUser(u, p) => authDB ! Write(u, p)
      case Authenticate(u, p) => handleAuthentication(u, p)

    }

    def handleAuthentication(u: String, p: String) = {
      val senderAuth = sender()
      val future = authDB ? Read(u)
      future.onComplete {
        //problem - who is sender()????!!!!!!! its future, so sender could change
        case Success(None) => senderAuth ! AuthFailure("not found")
        case Success(Some(pass)) =>
          if (pass == p) senderAuth ! AuthSuccess
          else senderAuth ! AuthFailure("incorrect")
        case _ => senderAuth ! AuthFailure("some error")
      }
    }
  }
    class PipedAuthManager extends AuthManager {
      override def handleAuthentication(u: String, p: String): Unit = {
        val future = authDB ? Read(u)
        val pasWordFuture = future.mapTo[Option[String]]
        val futureREsponse = pasWordFuture.map{
          case None => AuthFailure("incorrect")
          case Some(value) => if (value == p) AuthSuccess
          else AuthFailure("incorrect")
        }
        futureREsponse.pipeTo(sender())
      }
    }

}

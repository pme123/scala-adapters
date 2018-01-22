package pme123.adapters.server.control.mail

import akka.actor.{ActorSystem, Props}
import akka.routing.SmallestMailboxPool
import pme123.adapters.server.entity.MailMessage

/**
 * Created by pascal.mengelt on 13.03.2015.
 *
 * A MailService that uses Akka Actors and Apaches commons-email.
 * Inspired by http://raulraja.com/post/40997612883/sending-email-with-scala-and-akka-actors
 */
trait MailService {
  val nrOfInstances = 10

  lazy val  actorSystem = ActorSystem.create("MailService")
  /**
   * Uses the smallest inbox strategy to keep 10 instances alive ready to send out email
   * @see SmallestMailboxPool
   */
  lazy val mailServiceActor = actorSystem.actorOf(
    Props[MailServiceActor].withRouter(
      SmallestMailboxPool(nrOfInstances)
    ), name = "mailService"
  )

  /**
   * public interface to send out emails that dispatch the message to the listening actors
   */
  def send(mailMessage: MailMessage) {
    mailServiceActor ! mailMessage
  }

}

object MailService extends MailService

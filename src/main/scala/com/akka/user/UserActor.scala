package com.akka.user

import akka.actor.{Actor, ActorContext, ActorLogging, ActorPath, ActorRef, ActorSystem, Props}
import com.akka.data.Request
import com.akka.dispatcher.DispatcherActor.ProcessRequestFromUser
import com.akka.user.UserActor.{CreateUserActorWithId, DispatchMessageToDispatcher}

class UserActor(userId: Int) extends Actor with ActorLogging {
  override def receive: Receive = {

    case CreateUserActorWithId(userId) =>
      val userActor = context.actorOf(Props(new UserActor(userId)), "user-actor-" + userId)
      log.info("Created user actor {}", userActor.path.toString)

    case DispatchMessageToDispatcher(to, request) =>
      log.info("Message received from {}", context.self.path.toString)
      log.info("Message to be dispatched to {}", to.path.toString)
      to ! ProcessRequestFromUser(request)
  }

}


object UserActor {

  def userId(userId: Int): Props = Props(new UserActor(userId))

  sealed case class CreateUserActorWithId(userId: Int)

  sealed case class DispatchMessageToDispatcher(to: ActorRef, request: Request)
}
package com.akka.user

import akka.actor.{Actor, ActorContext, ActorLogging, ActorPath, ActorRef, ActorSystem, Props}
import com.akka.data.{Data, Request}
import com.akka.dispatcher.DispatcherActor.ProcessRequestFromUser
import com.akka.master.MasterActor.LoadFileToServer
import com.akka.user.UserActor.{AddFileToServer, CreateUserActorWithId, DispatchMessageToDispatcher}

class UserActor(userId: Int, serverActorSystem: ActorSystem) extends Actor with ActorLogging {
  override def receive: Receive = {

    case CreateUserActorWithId(userId) =>
      val userActor = context.actorOf(Props(new UserActor(userId, serverActorSystem)), "user-actor-" + userId)
      log.info("Created user actor {}", userActor.path.toString)

    case DispatchMessageToDispatcher(to, request) =>
      log.info("Message received from {}", context.self.path.toString)
      log.info("Message to be dispatched to {}", to.path.toString)
      to ! ProcessRequestFromUser(request)

    case AddFileToServer(data) =>
      val masterActor = serverActorSystem.actorSelection("akka://actor-system/user/master-actor")
      masterActor ! LoadFileToServer(data)
  }

}


object UserActor {


  sealed case class CreateUserActorWithId(userId: Int)

  sealed case class DispatchMessageToDispatcher(to: ActorRef, request: Request)

  sealed case class AddFileToServer(data: Data)
}
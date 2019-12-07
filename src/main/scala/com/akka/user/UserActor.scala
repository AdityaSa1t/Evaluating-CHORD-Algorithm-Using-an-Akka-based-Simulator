package com.akka.user

import akka.actor.{Actor, ActorContext, ActorLogging, ActorPath, ActorRef, ActorSystem, Props}
import com.akka.data.{Data, Request}
import com.akka.dispatcher.DispatcherActor.ProcessRequestFromUser
import com.akka.master.MasterActor.{LoadFileToServer, QueryDataFromServer}
import com.akka.user.UserActor.{AddFileToServer, CreateUserActorWithId, DispatchMessageToDispatcher, LookUpData}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout


class UserActor(userId: Int, serverActorSystem: ActorSystem) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)


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
      val future = masterActor ? LoadFileToServer(data)
      val result = Await.result(future, timeout.duration)
      sender() ! result


    case LookUpData(data) =>
      val masterActor = serverActorSystem.actorSelection("akka://actor-system/user/master-actor")
      val result = masterActor ? QueryDataFromServer(data)
  }

}


object UserActor {
  sealed case class CreateUserActorWithId(userId: Int)

  sealed case class DispatchMessageToDispatcher(to: ActorRef, request: Request)

  sealed case class AddFileToServer(data: Data)

  sealed case class LookUpData(data: Data)
}
package com.akka.user

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.akka.data.{Data, Request}
import com.akka.master.MasterActor.{LoadFileToServer, QueryDataFromServer}
import com.akka.user.UserActor.{AddFileToServer, CreateUserActorWithId, LookUpData}

import scala.concurrent.Await
import scala.concurrent.duration._


class UserActor(userId: Int, serverActorSystem: ActorSystem) extends Actor with ActorLogging {

  implicit val timeout = Timeout(10 seconds)


  override def receive: Receive = {

    // Creates a user with some id
    case CreateUserActorWithId(userId) =>
      val userActor = context.actorOf(Props(new UserActor(userId, serverActorSystem)), "user-actor-" + userId)
      log.info("Created user actor {}", userActor.path.toString)
      sender ! userActor.path

    // Add files to some server
    case AddFileToServer(data) =>
      val masterActor = serverActorSystem.actorSelection("akka://actor-system/user/master-actor")
      val future = masterActor ? LoadFileToServer(data)
      val result = Await.result(future, timeout.duration)
      sender() ! result

    // Look up
    case LookUpData(data) =>
      val masterActor = serverActorSystem.actorSelection("akka://actor-system/user/master-actor")
      val future_data = masterActor ? QueryDataFromServer(data)

      val result = Await.result(future_data, timeout.duration)
      sender() ! result
  }

}

object UserActor {
  sealed case class CreateUserActorWithId(userId: Int)

  sealed case class DispatchMessageToDispatcher(to: ActorRef, request: Request)

  sealed case class AddFileToServer(data: Data)

  sealed case class LookUpData(data: Data)
}
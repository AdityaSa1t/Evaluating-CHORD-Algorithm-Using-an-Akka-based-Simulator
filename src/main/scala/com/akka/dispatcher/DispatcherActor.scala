package com.akka.dispatcher

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.akka.data.Request
import com.akka.dispatcher.DispatcherActor.ProcessRequestFromUser
import com.akka.server.ServerActor.ProcessDispatcherMessage

class DispatcherActor(serverActorSystem: ActorSystem) extends Actor with ActorLogging {

  override def receive: Receive = {

    case ProcessRequestFromUser(request) =>
      log.info("In dispatcher {}", context.self.path.toString)
      val serverActor = serverActorSystem.actorSelection(request.path)
      serverActor ! ProcessDispatcherMessage
  }
}

object DispatcherActor {

  sealed case class ProcessRequestFromUser(request: Request)

  sealed case class GetDataFromServer()

}

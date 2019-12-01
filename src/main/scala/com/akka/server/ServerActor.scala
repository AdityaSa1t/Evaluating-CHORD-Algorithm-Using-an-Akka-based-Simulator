package com.akka.server

import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import com.akka.server.ServerActor.{CreateServerActorWithId, ProcessDispatcherMessage}

class ServerActor(serverId: Int) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ProcessDispatcherMessage =>
      log.info("Message received from dispatcher, in server with path {}", context.self.path.toString)

    case CreateServerActorWithId(serverId) =>
      val serverActor = context.actorOf(Props(new ServerActor(serverId)), "server-actor-" + serverId)
      log.info("Created server actor with path {}", serverActor.path.toString)
  }
}

object ServerActor {

  sealed case class ProcessDispatcherMessage()

  sealed case class CreateServerActorWithId(serverId: Int)

  def serverId(serverId: Int): Props = Props(new ServerActor(serverId))
}
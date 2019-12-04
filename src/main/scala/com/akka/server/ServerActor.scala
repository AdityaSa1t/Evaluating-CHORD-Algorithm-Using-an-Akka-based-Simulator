package com.akka.server

import akka.actor.{Actor, ActorLogging, Props}
import com.akka.server.ServerActor.{AddInitialMovieList, CreateServerActorWithId, ProcessDispatcherMessage}

import scala.collection.mutable

class ServerActor(serverId: Int, movieData: String) extends Actor with ActorLogging {

  private val movieList: mutable.ListBuffer[String] = new mutable.ListBuffer[String]

  override def receive: Receive = {

    case AddInitialMovieList(movieName) =>
      movieList += movieName
      log.info("In server with path {} and movieList {}", context.self.path, movieList)

    case ProcessDispatcherMessage =>
      log.info("Message received from dispatcher, in server with path {} and movie {} and size {}", context.self.path.toString, movieData, movieList.length)


    case CreateServerActorWithId(serverId, movieData) =>
      val serverActor = context.actorOf(ServerActor.serverId(serverId, movieData), "server-actor-" + serverId)
      serverActor ! AddInitialMovieList(movieData)
  }
}

object ServerActor {

  sealed case class AddInitialMovieList(movieName: String)

  sealed case class ProcessDispatcherMessage()

  sealed case class CreateServerActorWithId(serverId: Int, movieData: String)

  def serverId(serverId: Int, movieData: String): Props = Props(new ServerActor(serverId, movieData))
}
package com.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.akka.data.Data
import com.akka.master.MasterActor
import com.akka.server.ServerActor
import com.akka.server.ServerActor.CreateServerActorWithId
import com.akka.user.UserActor.{AddFileToServer, CreateUserActorWithId}
import com.akka.user.{DataUtil, UserActor}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.StdIn

/**
  * This singleton class represents a driver class which creates a system of Akka actors.
  **/
object ActorSystemDriver {

  private val usersConf = ConfigFactory.load("users.conf").getConfig("users-conf")
  private val serversConf = ConfigFactory.load("servers.conf").getConfig("servers-conf")
  private var future: Future[Any] = _
  implicit val timeout = Timeout(5 seconds)


  def main(args: Array[String]): Unit = {

    val movieData: ArrayBuffer[Data] = new mutable.ArrayBuffer[Data]

    val movies = DataUtil.returnData


    movies.indices.foreach {
      i =>
        movieData += Data(i, movies(i).movieName)
    }


    val numUsers = usersConf.getInt("num-users")
    val numServers = serversConf.getInt("num-servers")

    val actorSystem = ActorSystem("actor-system")

    val userActorSupervisor = actorSystem.actorOf(Props(new UserActor(1, actorSystem)), "user-actor-supervisor")
    val serverActorSupervisor = actorSystem.actorOf(Props(new ServerActor(1, numServers)), "server-actor-supervisor")
    val masterActor = actorSystem.actorOf(Props(new MasterActor(numServers)), "master-actor")


    (1 to numUsers).foreach {
      i =>
        userActorSupervisor ! CreateUserActorWithId(i + 1)
    }


    future = (1 to numServers).map {
      i =>
        serverActorSupervisor ? CreateServerActorWithId(i + 1, numServers)
    }.last


    val result = Await.result(future, timeout.duration).asInstanceOf[Int]

    if (result > 0) {

      val userActor = actorSystem.actorSelection("akka://actor-system/user/user-actor-supervisor/user-actor-3")
      userActor ! AddFileToServer(movieData(3))
      userActor ! AddFileToServer(movieData(23))
      userActor ! AddFileToServer(movieData(13))
      userActor ! AddFileToServer(movieData(31))
      userActor ! AddFileToServer(movieData(4))
      userActor ! AddFileToServer(movieData(0))
      userActor ! AddFileToServer(movieData(30))
      userActor ! AddFileToServer(movieData(41))
      userActor ! AddFileToServer(movieData(69))
      userActor ! AddFileToServer(movieData(15))
      userActor ! AddFileToServer(movieData(5))
      userActor ! AddFileToServer(movieData(7))
    }


    try {
      // Detect an external input to move to a new line
      StdIn.readLine
    } finally {
      // Terminates the user actor system
      actorSystem.terminate

    }
  }
}

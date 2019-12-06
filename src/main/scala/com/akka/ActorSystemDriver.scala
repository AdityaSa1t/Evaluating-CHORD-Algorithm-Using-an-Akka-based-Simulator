package com.akka

import akka.actor.{ActorSystem, Props}
import com.akka.data.Data
import com.akka.master.MasterActor
import com.akka.master.MasterActor.LoadFileToServer
import com.akka.server.ServerActor
import com.akka.server.ServerActor.CreateServerActorWithId
import com.akka.user.UserActor.CreateUserActorWithId
import com.akka.user.{DataUtil, UserActor}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn
import akka.pattern._

/**
 * This singleton class represents a driver class which creates a system of Akka actors.
 **/
object ActorSystemDriver {

  private val usersConf = ConfigFactory.load("users.conf").getConfig("users-conf")
  private val serversConf = ConfigFactory.load("servers.conf").getConfig("servers-conf")

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



    val future = (1 to numUsers).foreach {

      i =>
        userActorSupervisor ! CreateUserActorWithId(i + 1)
    }

    val result = (1 to numServers).foreach {
      i =>
        serverActorSupervisor ? CreateServerActorWithId(i + 1, numServers)
    }



    masterActor ! LoadFileToServer(movieData(3))
    masterActor ! LoadFileToServer(movieData(6))
    masterActor ! LoadFileToServer(movieData(2))
    masterActor ! LoadFileToServer(movieData(19))
    masterActor ! LoadFileToServer(movieData(20))



    try {
      // Detect an external input to move to a new line
      StdIn.readLine
    } finally {
      // Terminates the user actor system
      actorSystem.terminate

    }
  }
}

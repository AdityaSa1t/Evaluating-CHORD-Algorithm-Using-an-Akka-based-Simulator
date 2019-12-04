package com.akka

import akka.actor.{ActorSystem, Props}
import com.akka.data.{Data, Request}
import com.akka.dispatcher.DispatcherActor
import com.akka.server.ServerActor
import com.akka.server.ServerActor.CreateServerActorWithId
import com.akka.user.UserActor
import com.akka.user.UserActor.{CreateUserActorWithId, DispatchMessageToDispatcher}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn

/**
  * This singleton class represents a driver class which creates a system of Akka actors.
  **/
object ActorSystemDriver {

  private val usersConf = ConfigFactory.load("users.conf").getConfig("users-conf")
  private val serversConf = ConfigFactory.load("servers.conf").getConfig("servers-conf")

  def main(args: Array[String]): Unit = {

    val movieData: ArrayBuffer[Data] = new mutable.ArrayBuffer[Data]

    movieData += Data(1, "Titanic")
    movieData += Data(2, "Shawshank Redemption")
    movieData += Data(3, "Hera Pheri")
    movieData += Data(4, "Count of Monte Cristo")
    movieData += Data(5, "V for Vendetta")


    val userActorSystem = ActorSystem("user-actor-system")
    val dispatcherActorSystem = ActorSystem("dispatcher-actor-system")
    val serverActorSystem = ActorSystem("server-actor-system")

    val userActorSupervisor = userActorSystem.actorOf(Props(new UserActor(1)), "user-actor-supervisor")
    val serverActorSupervisor = serverActorSystem.actorOf(Props(new ServerActor(1, "")), "server-actor-supervisor")
    val dispatcherActor = dispatcherActorSystem.actorOf(Props(new DispatcherActor(serverActorSystem)), "dispatcher-actor")


    (1 to usersConf.getInt("num-users")).foreach {
      i =>
        userActorSupervisor ! CreateUserActorWithId(i + 1)
    }

    (1 to serversConf.getInt("num-servers")).foreach {
      i =>
        val dataList = new mutable.ArrayBuffer[Data]()
        dataList += movieData(i - 1)
        serverActorSupervisor ! CreateServerActorWithId(i + 1, dataList(0).movieName)
    }

    val userActor = userActorSystem.actorSelection("akka://user-actor-system/user/user-actor-supervisor/user-actor-11")
    userActor ! DispatchMessageToDispatcher(dispatcherActor, Request("akka://server-actor-system/user/server-actor-supervisor/server-actor-3"))
    userActor ! DispatchMessageToDispatcher(dispatcherActor, Request("akka://server-actor-system/user/server-actor-supervisor/server-actor-2"))
    userActor ! DispatchMessageToDispatcher(dispatcherActor, Request("akka://server-actor-system/user/server-actor-supervisor/server-actor-6"))
    userActor ! DispatchMessageToDispatcher(dispatcherActor, Request("akka://server-actor-system/user/server-actor-supervisor/server-actor-4"))
    userActor ! DispatchMessageToDispatcher(dispatcherActor, Request("akka://server-actor-system/user/server-actor-supervisor/server-actor-5"))


    try {

      // Detect an external input to move to a new line
      StdIn.readLine

    } finally {

      // Terminates the user actor system
      userActorSystem.terminate
      serverActorSystem.terminate
      dispatcherActorSystem.terminate
    }
  }
}

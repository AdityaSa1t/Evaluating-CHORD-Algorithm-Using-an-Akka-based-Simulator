package com.akka

import akka.actor.ActorSystem
import com.akka.user.UserActor
import com.akka.user.UserActor.CreateUserActorWithId
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
  * This singleton class represents a driver class which creates a system of Akka actors.
  * */
object ActorSystemDriver {

  def main(args: Array[String]): Unit = {

    // Create a system of user actors
    val userActorSystem = ActorSystem("user-actors")

    // Load the no. of user actors
    val numUsers = ConfigFactory.load("users.conf").getConfig("users-conf").getInt("num-users")

    // Create a supervisor user actor
    val userActorSupervisor = userActorSystem.actorOf(UserActor.userId(1), "user-supervisor")

    (1 to numUsers).foreach {
      i =>
        // Creates other user actors as children of the supervisor user actor
        userActorSupervisor ! CreateUserActorWithId(i + 1)
    }

    try {

      // Detect an I/O input to move to a new line
      StdIn.readLine
    } finally {

      // Terminates the user actor system
      userActorSystem.terminate
    }
  }
}

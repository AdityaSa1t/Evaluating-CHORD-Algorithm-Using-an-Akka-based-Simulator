package com.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ActorSystemDriver {

  def main(args: Array[String]): Unit = {
    val userActorSystem = ActorSystem("user-actors")
    val usersConfig = ConfigFactory.load("users.conf").getConfig("users-conf")

    (1 to usersConfig.getInt("num-users")).foreach{
      id =>

    }

  }
}

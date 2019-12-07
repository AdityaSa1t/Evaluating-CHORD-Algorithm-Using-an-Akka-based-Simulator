package com.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.akka.data.Data
import com.akka.master.MasterActor
import com.akka.master.MasterActor.CreateSnapshot
import com.akka.server.ServerActor
import com.akka.server.ServerActor.CreateServerActorWithId
import com.akka.user.UserActor.{AddFileToServer, CreateUserActorWithId, LookUpData}
import com.akka.user.{DataUtil, UserActor}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
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
      val futureForAdd = userActor ? AddFileToServer(movies(3))
      userActor ? AddFileToServer(movies(23))
      userActor ? AddFileToServer(movies(13))
      userActor ? AddFileToServer(movies(14))
      userActor ? AddFileToServer(movies(15))
      userActor ? AddFileToServer(movies(16))
      userActor ? AddFileToServer(movies(27))
      userActor ? AddFileToServer(movies(37))
      userActor ? AddFileToServer(movies(9))
      userActor ? AddFileToServer(movies(50))
      userActor ? AddFileToServer(movies(51))
      userActor ? AddFileToServer(movies(48))




      val resultForAdd = Await.result(futureForAdd, timeout.duration).asInstanceOf[ListBuffer[Data]]

      println(resultForAdd)

      if (resultForAdd.nonEmpty) {

        userActor ! LookUpData(movies(13))
        userActor ! LookUpData(movies(23))
        userActor ! LookUpData(movies(3))
        userActor ! LookUpData(movies(48))
        userActor ! LookUpData(movies(9))
        userActor ! LookUpData(movies(37))

      }
    }

    if (result > 0) {
      masterActor ! CreateSnapshot
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

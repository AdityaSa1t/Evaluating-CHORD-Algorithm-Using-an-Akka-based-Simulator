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
import com.akka.user.UserActor
import com.akka.utils.DataUtil
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * This singleton class represents a driver class which creates a system of Akka actors.
  **/
object ActorSystemDriver extends LazyLogging {


  private val usersConf = ConfigFactory.load("users.conf").getConfig("users-conf")
  private val serversConf = ConfigFactory.load("servers.conf").getConfig("servers-conf")
  private var future: Future[Any] = _
  implicit val timeout = Timeout(5 seconds)

  val actorSystem = ActorSystem("actor-system")


  val numUsers = usersConf.getInt("num-users")
  val numServers = serversConf.getInt("num-servers")
  var numServersCreated = 0


  val userActorSupervisor = actorSystem.actorOf(Props(new UserActor(1, actorSystem)), "user-actor-supervisor")
  val serverActorSupervisor = actorSystem.actorOf(Props(new ServerActor(1, numServers)), "server-actor-supervisor")
  val masterActor = actorSystem.actorOf(Props(new MasterActor(numServers)), "master-actor")
  val r = scala.util.Random

  (1 to numUsers).foreach {
    i =>
      userActorSupervisor ! CreateUserActorWithId(i + 1)
  }

  val movieData: ArrayBuffer[Data] = new mutable.ArrayBuffer[Data]

  val movies = DataUtil.returnData


  movies.indices.foreach {
    i =>
      movieData += Data(i, movies(i).movieName)
  }


  def createNode(): Boolean = {
    if (numServersCreated < numServers) {
      serverActorSupervisor ? CreateServerActorWithId(numServersCreated, numServers)
      numServersCreated += 1
      return true
    }
    else {
      logger.info("Cant create more Servers!")
    }

    false
  }

  def loadData(movie_index: Int): Unit = {
    val userActor = actorSystem.actorSelection("akka://actor-system/user/user-actor-supervisor/user-actor-3")
    //userActor ! AddFileToServer(movieData(r.nextInt(movieData.length)))
    userActor ! AddFileToServer(movieData(movie_index))
  }

  def lookUpData(movie_id: Int): Any = {
    val userActor = actorSystem.actorSelection("akka://actor-system/user/user-actor-supervisor/user-actor-3")
    val data = userActor ? LookUpData(movies(movie_id))
    val result = Await.result(data, timeout.duration)
    result
  }

  def getSnapshot(): Any = {
    val snap_data = masterActor ? CreateSnapshot
    val result = Await.result(snap_data, timeout.duration)
    result
  }

}

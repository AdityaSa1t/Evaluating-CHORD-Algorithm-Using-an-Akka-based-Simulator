package com.akka.master

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.akka.data.Data
import com.akka.master.MasterActor.{AddNodeToRing, LoadFileToServer, QueryDataFromServer, RemoveServerFromRing}
import com.akka.server.ServerActor.{Deactivate, GetData, LoadData, UpdateFingerTable}
import com.akka.utils.HashUtils

import scala.collection.mutable
import scala.concurrent.Await
import akka.pattern.ask

import scala.concurrent.duration._


class MasterActor(maxNodesInRing: Int) extends Actor with ActorLogging {

  private var numNodesInRing: Int = 0
  private val serverActorHashedTreeSet = new mutable.TreeSet[Int]()
  private val contextPaths = new mutable.ListBuffer[String]
  private val mapContextToHash = new mutable.HashMap[Int, String]()
  implicit val timeout = Timeout(5 seconds)


  override def receive: Receive = {
    case AddNodeToRing(hashKey, serverActorPath) =>

      numNodesInRing += 1
      serverActorHashedTreeSet += hashKey.toInt
      contextPaths += serverActorPath
      mapContextToHash += (hashKey.toInt -> serverActorPath)

      contextPaths.foreach {
        path =>
          val serverActor = context.system.actorSelection(path)
          serverActor ! UpdateFingerTable(serverActorHashedTreeSet)
      }

      log.info("Node added to ring, num nodes {}", numNodesInRing)
      log.info("Hashed set : {}", serverActorHashedTreeSet)
      log.info("Context paths : {}", contextPaths)

      sender() ! contextPaths.size


    case LoadFileToServer(data) =>
      val dataHashedValue = HashUtils.generateHash(data.id.toString, maxNodesInRing, "SHA-1")
      log.info("Size of server actor hashed tree set : {}", serverActorHashedTreeSet.size)
      val tempTreeSet = serverActorHashedTreeSet.filter(x => x >= dataHashedValue.toInt)
      val serverHash = if (tempTreeSet.nonEmpty) {
        tempTreeSet.head
      } else {
        serverActorHashedTreeSet.head
      }
      val serverActor = context.system.actorSelection(mapContextToHash(serverHash))
      val future = serverActor ? LoadData(data)
      val result = Await.result(future, timeout.duration)
      sender() ! result


    case QueryDataFromServer(data) =>
      log.info("Querying data {} from server", data)
      val serverActor = context.system.actorSelection(contextPaths(0))
      serverActor ! GetData(data, mapContextToHash, serverActorHashedTreeSet)

  }
}

object MasterActor {

  sealed case class AddNodeToRing(hashKey: String, serverActorPath: String)

  sealed case class LoadFileToServer(data: Data)

  sealed case class QueryDataFromServer(data: Data)

  sealed case class InitializationDone()

  sealed case class RemoveServerFromRing(serverId: Int)
}

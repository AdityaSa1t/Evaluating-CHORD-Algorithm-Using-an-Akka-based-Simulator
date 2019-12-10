package com.akka.master

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.akka.data.Data
import com.akka.master.MasterActor._
import com.akka.server.ServerActor._
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


      // Routes user request to some server
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


      // Routes user query for looking up some data
    case QueryDataFromServer(data) =>
      log.info("Querying data {} from server", data)
      val serverActor = context.system.actorSelection(contextPaths(0))
      val future_data = serverActor ? GetData(data, mapContextToHash, serverActorHashedTreeSet)
      val result = Await.result(future_data, timeout.duration)
      sender() ! result

      // Creates the global view of the system
    case CreateSnapshot =>
      val snapshotData: mutable.ListBuffer[String] = new mutable.ListBuffer[String]()
      log.info("Snapshot of total no. of servers in the ring : {}", numNodesInRing)
      contextPaths.foreach {
        contextPath =>
          val serverActor = context.actorSelection(contextPath)
          var snapData =  (serverActor ? Snapshot)
          var result = Await.result(snapData, timeout.duration)
          snapshotData += result.toString
      }


      sender() ! snapshotData
  }
}

object MasterActor {

  sealed case class AddNodeToRing(hashKey: String, serverActorPath: String)

  sealed case class LoadFileToServer(data: Data)

  sealed case class QueryDataFromServer(data: Data)

  sealed case class InitializationDone()

  sealed case class RemoveServerFromRing(serverId: Int)

  sealed case class CreateSnapshot()

}

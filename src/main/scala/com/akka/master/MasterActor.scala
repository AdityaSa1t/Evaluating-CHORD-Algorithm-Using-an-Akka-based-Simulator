package com.akka.master

import akka.actor.{Actor, ActorLogging}
import com.akka.master.MasterActor.AddNodeToRing
import com.akka.server.ServerActor.UpdateFingerTable

import scala.collection.mutable

class MasterActor extends Actor with ActorLogging {

  private var numNodesInRing: Int = 0
  private val serverActorHashedTreeSet = new mutable.TreeSet[String]()
  private val contextPaths = new mutable.ListBuffer[String]

  override def receive: Receive = {
    case AddNodeToRing(hashKey, serverActorPath) =>
      numNodesInRing += 1
      serverActorHashedTreeSet += hashKey
      contextPaths += serverActorPath
      contextPaths.foreach {
        path =>
          val serverActor = context.system.actorSelection(path)
          serverActor ! UpdateFingerTable(serverActorHashedTreeSet)
      }
      log.info("Node added to ring")

  }
}

object MasterActor {
  sealed case class AddNodeToRing(hashKey: String, serverActorPath: String)
}

package com.akka.server

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.akka.data.{Data, FingerTableEntry}
import com.akka.master.MasterActor.AddNodeToRing
import com.akka.server.ServerActor._
import com.akka.utils.HashUtils

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class ServerActor(serverId: Int, maxFingerTableEntries: Int) extends Actor with ActorLogging {

  private val movieList: mutable.ListBuffer[Data] = new mutable.ListBuffer[Data]
  private var fingerTable = new mutable.HashMap[Int, FingerTableEntry]
  private var hashedValue: Int = -1

  implicit val timeout = Timeout(5 seconds)


  override def receive: Receive = {

    case InitFingerTable(hashedValue) =>
      (0 until maxFingerTableEntries).foreach {
        i =>
          fingerTable += (i -> FingerTableEntry(((hashedValue.toInt + scala.math.pow(2, i)) % scala.math.pow(2, maxFingerTableEntries)).asInstanceOf[Int], hashedValue.toInt))
      }
      this.hashedValue = hashedValue.toInt


    case CreateServerActorWithId(serverId, maxFingerTableEntries) =>
      val serverActor = context.actorOf(ServerActor.serverId(serverId, maxFingerTableEntries), "server-actor-" + serverId)
      serverActor ! InitFingerTable
      log.info("Created server with path {}", serverActor.path)
      val masterActor = context.system.actorSelection("akka://actor-system/user/master-actor")
      hashedValue = HashUtils.generateHash(serverId.toString, maxFingerTableEntries, "SHA-1").toInt
      serverActor ! InitFingerTable(hashedValue.toString)

      val numNodes = masterActor ? AddNodeToRing(hashedValue.toString, serverActor.path.toString)

      val result = Await.result(numNodes, timeout.duration)
      sender() ! result

    case UpdateFingerTable(hashedNodes) =>
      fingerTable = ServerActor.setSuccessor(hashedNodes, fingerTable)
      log.info("Finger table updated : {}", fingerTable)

    case LoadData(data) =>
        movieList += data
        log.info("Loaded data {} in server with path {}", data, context.self.path)
        sender() ! movieList

    case GetData(data, serverToContextMap, serverActorHashedTreeSet) =>
      val hashedDataVal = HashUtils.generateHash(data.id.toString, maxFingerTableEntries, "SHA-1").toInt
      val self = context.self.path
      if (movieList.contains(data)) {
        log.info("Found data {} in server {}", data, context.self.path)
      } else {
        val possibleDestinations = fingerTable.map {
          entry =>
            entry._2.actualSuccessorId
        }.toList.filter(x => x >= hashedDataVal).sorted

        if (possibleDestinations.nonEmpty && possibleDestinations.head >= hashedDataVal) {
          val serverActor = context.system.actorSelection(serverToContextMap(possibleDestinations.head))
          serverActor ! GetData(data, serverToContextMap, serverActorHashedTreeSet)
        }
        else {

          val tempTreeSet = serverActorHashedTreeSet.filter(x => x >= hashedValue)

          if (tempTreeSet.isEmpty) {
            val serverActor = context.system.actorSelection(serverToContextMap(serverActorHashedTreeSet.head))
            serverActor ! GetData(data, serverToContextMap, serverActorHashedTreeSet)
          } else {
            val serverActor = context.system.actorSelection(serverToContextMap(tempTreeSet.head))
            serverActor ! GetData(data, serverToContextMap, serverActorHashedTreeSet)
          }
        }
      }


  }
}

object ServerActor {

  sealed case class InitFingerTable(hashedValue: String)

  sealed case class CreateServerActorWithId(serverId: Int, maxFingerTableEntries: Int)

  sealed case class UpdateFingerTable(hashedNodes: mutable.TreeSet[Int])

  sealed case class LoadData(data: Data)

  sealed case class GetData(data: Data, serverToContextMap: mutable.HashMap[Int, String], serverActorHashedTreeSet: mutable.TreeSet[Int])

  sealed case class Deactivate(serverId: Int)

  def serverId(serverId: Int, maxFingerTableEntries: Int): Props = Props(new ServerActor(serverId, maxFingerTableEntries))

  def setSuccessor(treeSet: mutable.TreeSet[Int], fingerTableEntry: mutable.HashMap[Int, FingerTableEntry]): mutable.HashMap[Int, FingerTableEntry] = {
    val updatedFingerTable = fingerTableEntry.map {
      entry =>
        val tempTreeSet = treeSet.filter(x => x > entry._2.successorId)

        if (tempTreeSet.nonEmpty) {
          entry._1 -> FingerTableEntry(entry._2.successorId, tempTreeSet.head)
        } else {
          entry._1 -> FingerTableEntry(entry._2.successorId, treeSet.head)
        }
    }
    updatedFingerTable
  }

}
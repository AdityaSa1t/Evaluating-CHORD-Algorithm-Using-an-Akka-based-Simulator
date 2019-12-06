package com.akka.server

import akka.actor.{Actor, ActorLogging, Props}
import com.akka.data.FingerTableEntry
import com.akka.master.MasterActor.AddNodeToRing
import com.akka.server.ServerActor.{AddInitialMovieList, CreateServerActorWithId, InitFingerTable, UpdateFingerTable}
import com.akka.utils.HashUtils

import scala.collection.mutable

class ServerActor(serverId: Int, maxFingerTableEntries: Int) extends Actor with ActorLogging {

  private val movieList: mutable.ListBuffer[String] = new mutable.ListBuffer[String]
  private var fingerTable = new mutable.HashMap[Int, FingerTableEntry]

  override def receive: Receive = {

    case InitFingerTable =>
      (0 until maxFingerTableEntries).foreach {
        i =>
          fingerTable += (i -> FingerTableEntry(((serverId + scala.math.pow(2, i)) % scala.math.pow(2, maxFingerTableEntries)).asInstanceOf[Int], serverId))
      }
      log.info("Finger table built for server with path {} with size {}", context.self.path, fingerTable.size)

    case AddInitialMovieList(movieName) =>
      movieList += movieName
      log.info("In server with path {} and movieList {}", context.self.path, movieList)

    case CreateServerActorWithId(serverId, maxFingerTableEntries) =>
      val serverActor = context.actorOf(ServerActor.serverId(serverId, maxFingerTableEntries), "server-actor-" + serverId)
      serverActor ! InitFingerTable
      log.info("Created server with path {}", serverActor.path)
      val masterActor = context.system.actorSelection("akka://server-actor-system/user/master-actor")
      masterActor ! AddNodeToRing(HashUtils.generateHash(serverId.toString, maxFingerTableEntries, "SHA-1"), serverActor.path.toString)

    case UpdateFingerTable(hashedNodes) =>
      val treeSet = new mutable.TreeSet[Int]()
      hashedNodes.foreach {
        node =>
          treeSet += Integer.parseInt(node, 10)
      }
      fingerTable = ServerActor.setSuccessor(treeSet, fingerTable)
      log.info("Updated finger table : {}", fingerTable)


  }
}

object ServerActor {

  sealed case class InitFingerTable()

  sealed case class AddInitialMovieList(movieName: String)

  sealed case class CreateServerActorWithId(serverId: Int, maxFingerTableEntries: Int)

  sealed case class UpdateFingerTable(hashedNodes: mutable.TreeSet[String])

  def serverId(serverId: Int, maxFingerTableEntries: Int): Props = Props(new ServerActor(serverId, maxFingerTableEntries))

  def setSuccessor(treeSet: mutable.TreeSet[Int], fingerTableEntry: mutable.HashMap[Int, FingerTableEntry]): mutable.HashMap[Int, FingerTableEntry] = {
    fingerTableEntry.map {
      entry =>
        val tempTreeSet = treeSet.filter(x => x > entry._2.successorId)

        if (tempTreeSet.nonEmpty) {
          entry._1 -> FingerTableEntry(entry._2.successorId, tempTreeSet.head)
        } else {
          entry
        }
    }
    fingerTableEntry
  }

}
package com.akka.server

import akka.actor.{Actor, ActorLogging, Props}
import com.akka.data.{Data, FingerTableEntry}
import com.akka.master.MasterActor.AddNodeToRing
import com.akka.server.ServerActor._
import com.akka.utils.HashUtils

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.Await

class ServerActor(serverId: Int, maxFingerTableEntries: Int) extends Actor with ActorLogging {

  private val movieList: mutable.ListBuffer[Data] = new mutable.ListBuffer[Data]
  private var fingerTable = new mutable.HashMap[Int, FingerTableEntry]

  override def receive: Receive = {

    case InitFingerTable(hashedValue) =>
      (0 until maxFingerTableEntries).foreach {
        i =>
          fingerTable += (i -> FingerTableEntry(((hashedValue.toInt + scala.math.pow(2, i)) % scala.math.pow(2, maxFingerTableEntries)).asInstanceOf[Int], hashedValue.toInt))
      }
      log.info("Finger table built for server with path {} with size {}", context.self.path, fingerTable.size)


    case CreateServerActorWithId(serverId, maxFingerTableEntries) =>
      val serverActor = context.actorOf(ServerActor.serverId(serverId, maxFingerTableEntries), "server-actor-" + serverId)
      serverActor ! InitFingerTable
      log.info("Created server with path {}", serverActor.path)
      val masterActor = context.system.actorSelection("akka://server-actor-system/user/master-actor")
      val hashedValue = HashUtils.generateHash(serverId.toString, maxFingerTableEntries, "SHA-1")
      serverActor ! InitFingerTable(hashedValue)

      implicit val timeout = Timeout(5 seconds)
      var no_of_nodes = masterActor ? AddNodeToRing(hashedValue, serverActor.path.toString)

      val result = Await.result(no_of_nodes, timeout.duration)
      println("Server Actor -> " +result)

      sender() ! no_of_nodes


    case UpdateFingerTable(hashedNodes) =>
      fingerTable = ServerActor.setSuccessor(hashedNodes, fingerTable)

    case LoadData(data) =>
      movieList += data
      log.info("Loaded data in server with path {}", context.self.path)


  }
}

object ServerActor {

  sealed case class InitFingerTable(hashedValue: String)

  sealed case class CreateServerActorWithId(serverId: Int, maxFingerTableEntries: Int)

  sealed case class UpdateFingerTable(hashedNodes: mutable.TreeSet[Int])

  sealed case class LoadData(data: Data)

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
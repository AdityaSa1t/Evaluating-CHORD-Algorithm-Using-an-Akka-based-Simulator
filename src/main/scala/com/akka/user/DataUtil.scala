package com.akka.user

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

import scala.collection.mutable.ListBuffer
import scala.io.Source

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object DataUtil extends App {

  def returnData:List[String]={
    val res: ListBuffer[String] = new ListBuffer[String]
    val lines = Source.fromFile("./src/main/resources/data.csv")
    for (line <- lines.getLines.drop(1)) {
      val cols = line.split(",")
      res += cols(0)
    }
    res.toList
    }

 /* def main:Unit={
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http  "+returnData.toString()+" </h1>"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/ \n\n\tPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }*/

}


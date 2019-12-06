package com.akka.user

import com.akka.data.Data

import scala.collection.mutable.ListBuffer
import scala.io.Source

object DataUtil extends App {

  def returnData:List[Data]={
    val res: ListBuffer[String] = new ListBuffer[String]
    val resData: ListBuffer[Data] = new ListBuffer[Data]
    val lines = Source.fromFile("./src/main/resources/data.csv")
    var i : Int= 0
    for (line <- lines.getLines.drop(1)) {
      val cols = line.split(",")
      resData += Data(i,cols(0))
      i = i+1
    }
    resData.toList
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


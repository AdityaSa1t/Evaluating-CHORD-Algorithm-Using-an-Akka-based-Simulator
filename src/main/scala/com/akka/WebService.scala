package com.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer


import scala.io.StdIn

object WebService {
  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    var serverNodeCreated = false
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      get {
        concat(
          pathSingleSlash {
            ActorSystemDriver
            complete(HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              "<html><body> <a href=\"http://127.0.0.1:8080/addNode\">1. Add a Server Node</a><br> " +
                "<a href=\"http://127.0.0.1:8080/loadData\">2. Load Data to Servers by Id</a><br> " +
                "<a href=\"http://127.0.0.1:8080/lookupData\">3. Lookup Data on Servers by Id</a><br> " +
                "</body></html>"))
          },


          path("ping") {
            parameters('id) { (id) =>
              println(id)
              complete(id)
            }
          },


          //Route definition for adding a node to the server.
          path("addNode") {
            val result = ActorSystemDriver.createNode()

            if (result) {
              serverNodeCreated = true
              complete(HttpResponse(entity = HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                "<html><body> Added a node! <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br><br> </body></html>")))

            }
            else
              complete(HttpResponse(entity = "Can't add more servers to the system."))
          },


          //Route definition for loading movie data to a node.
          path("loadData") {
            parameters('id) { (id) =>
              if (serverNodeCreated) {
                if (id.toInt >= ActorSystemDriver.movieData.length) {
                  complete("Enter id between 0 and " + (ActorSystemDriver.movieData.length - 1) + "")
                }
                else {
                  ActorSystemDriver.loadData(id.toInt)
                  complete(HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body> Loaded Data. <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br> </body></html>")))
                }
              } else {
                complete(HttpResponse(entity = HttpEntity(
                  ContentTypes.`text/html(UTF-8)`,
                  "<html><body> Add a server node first! <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br> </body></html>")))
              }
            }
          },



          //Route definition for lookup.
          path("lookupData") {
            parameters('id) { (id) =>
              if(serverNodeCreated) {
                ActorSystemDriver.lookUpData(id.toInt)
                complete("Done Lookup!")

              } else {
                complete(HttpResponse(entity = HttpEntity(
                  ContentTypes.`text/html(UTF-8)`,
                  "<html><body> Add a server node first! <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br> </body></html>")))
              }
            }
          },


          path("crash") {
            sys.error("BOOM!")
          }
        )
      }

    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

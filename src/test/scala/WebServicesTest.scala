import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.akka.ActorSystemDriver
import org.scalatest.{Matchers, WordSpec}

class WebServicesTest extends WordSpec with Matchers with ScalatestRouteTest {

  val content = "<html><body> <a href=\"http://127.0.0.1:8080/addNode\">1. Add a Server Node</a><br> " +
    "<a href=\"http://127.0.0.1:8080/loadData\">2. Load Data to Servers by Id</a><br> " +
    "<a href=\"http://127.0.0.1:8080/lookupData\">3. Lookup Data on Servers by Id</a><br> " +
    "</body></html>"

  val nodeAddResult = "<html><body> Added a node! <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br><br> </body></html>"

  val actorSystemDriver = new ActorSystemDriver

  val trialRoute=get {
    concat(
      pathSingleSlash {
        complete(HttpEntity(
          ContentTypes.`text/html(UTF-8)`,content))
      },

      path("addNode") {
        val result = actorSystemDriver.createNode()

        if (result) {
          complete(HttpResponse(entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<html><body> Added a node! <br><a href=\"http://127.0.0.1:8080/\">Go Back</a><br><br> </body></html>")))
        }
        else
          complete(HttpResponse(entity = "Can't add more servers to the system."))
      },

      path("ping") {
        parameters('id) { (id) =>
          println(id)
          complete(id)
        }
      },

      path("crash") {
        sys.error("BOOM!")
      }
    )
  }

  "The service" should {

    "return entry point content/options for GET requests to the root path" in {
      // tests:
      Get() ~> trialRoute ~> check {
        responseAs[String] shouldEqual content
      }
    }

    "return acknowledgement that a node has been added" in{
      Get("/addNode") ~> trialRoute ~> check {
        responseAs[String] shouldEqual nodeAddResult
      }
    }

  }

}

import akka.actor.{ActorPath, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.akka.master.MasterActor
import com.akka.user.UserActor
import com.akka.utils.{DataUtil, HashUtils}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, MustMatchers}

class ActorSystemTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike with BeforeAndAfterAll with MustMatchers {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "Actor System" should "create user actor with a selection" in {
    val sender = TestProbe()

    val index: Int = 101
    val actorSystem = ActorSystem("actor-system")
    val user = actorSystem.actorOf(Props(new UserActor(index, actorSystem)))
    sender.send(user, UserActor.CreateUserActorWithId(index))

    val state = sender.expectMsgType[ActorPath]

    assert(null != state)
  }

  "User Actor" should "have the correct actor selection as part of the actor path" in {
    val sender = TestProbe()

    val index: Int = 202
    val actorSystem = ActorSystem("actor-system")
    val user = actorSystem.actorOf(Props(new UserActor(index, actorSystem)))
    sender.send(user, UserActor.CreateUserActorWithId(index))

    val state = sender.expectMsgType[ActorPath]

    assert(state.toString.contains(index.toString) && state.isInstanceOf[ActorPath] && state.toString.contains("user"))
  }

  "DataUtil" should "return a list of unique movies" in {
    val resultList = DataUtil.returnData
    val verifyList = resultList.distinct
    assert(resultList.length > 0 && resultList == verifyList)
  }

  "HashUtil" should " convert any string to hashed m-bit unsigned integer" in {
    val numbits: Int = 15
    val algo = "SHA-1"
    val result = HashUtils.generateHash("Cloud Computing Objects: The final chapter", numbits, algo)
    assert(result.toInt < math.pow(2, numbits))
  }

  "Master Actor" should "return ring size." in {

    val sender = TestProbe()
    val numbits: Int = 15
    val algo = "SHA-1"

    val result = HashUtils.generateHash("Cloud Computing Objects: The final chapter", numbits, algo)

    val actorSystem = ActorSystem("actor-system")
    val user = actorSystem.actorOf(Props(new UserActor(numbits, actorSystem)))

    val master = actorSystem.actorOf(Props(new MasterActor(5)))

    sender.send(master, MasterActor.AddNodeToRing(result, user.path.toString))

    val state = sender.expectMsgType[Int]
    assert(state > 0)

  }


}

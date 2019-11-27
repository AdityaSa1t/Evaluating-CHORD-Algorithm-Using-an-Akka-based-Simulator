package com.akka.user

import akka.actor.{Actor, ActorLogging, Props}
import com.akka.user.UserActor.CreateUserActorWithId

/**
  * This class denotes a user actor.
  * */
class UserActor(id: Int) extends Actor with ActorLogging {

  override def receive = {

    case CreateUserActorWithId(userId) => {

      log.info("Response to create user actor with id : {} received", userId)

      // Create a child actor
      val userActor = context.actorOf(UserActor.userId(userId), "user-actor-" + userId)

      log.info("User actor {} created", userActor.path.toString)
    }
  }

}

/**
  * This singleton class is a companion to UserActor. It defines all possible actions defines on an instance of a UserActor.
  * */
object UserActor {

  // Sealed class which denotes a message to spawn a user actor with some user id
  sealed case class CreateUserActorWithId(userId: Int)

  // Immutable user id property, try to ensure this property is unique across all users
  def userId(id: Int): Props = Props(new UserActor(id))
}
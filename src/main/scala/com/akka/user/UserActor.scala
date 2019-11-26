package com.akka.user

import akka.actor.{Actor, ActorLogging, Props}
import com.akka.user.UserActor.{GetResponseForWriteOrDelete, GetStockData}

class UserActor(id: Int) extends Actor with ActorLogging  {
  override def receive = {
    case GetStockData(stockData) => log.info("Stock data received : {}", stockData)
    case GetResponseForWriteOrDelete(response) => log.info("Response received : {}", response)
  }
}

object UserActor {

  sealed case class GetStockData(stockData: String)
  sealed case class GetResponseForWriteOrDelete(response: String)

  def userId(id: Int): Props = Props(new UserActor(id))
}
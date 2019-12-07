package com.akka.utils

import com.akka.data.Data

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
 * A utility class which reads records from the csv file.
 **/
object DataUtil extends App {

  def returnData: List[Data] = {
    val res: ListBuffer[String] = new ListBuffer[String]
    val resData: ListBuffer[Data] = new ListBuffer[Data]
    val lines = Source.fromFile("./src/main/resources/data.csv")
    var i: Int = 0
    for (line <- lines.getLines.drop(1)) {
      val cols = line.split(",")
      resData += Data(i, cols(0))
      i = i + 1
    }
    resData.toList
  }
}

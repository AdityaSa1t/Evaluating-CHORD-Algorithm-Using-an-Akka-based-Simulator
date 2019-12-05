package com.akka.utils

object HashUtils {
  def generateHash(uniqueId: String, numBits: Int, hashingAlgorithm: String) = {
    val md = java.security.MessageDigest.getInstance(hashingAlgorithm).digest(uniqueId.getBytes("UTF-8")).map("%02x".format(_)).mkString
    md.substring(0, numBits)
  }
}

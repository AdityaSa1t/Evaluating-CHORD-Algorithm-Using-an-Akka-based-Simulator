import java.security.MessageDigest
import com.akka.utils.HashUtils

object Hashing {
  def main(args: Array[String]): Unit = {
    val id = 1
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val hash = md.digest(id.toString.getBytes("UTF-8")).map("%02x".format(_)).mkString
    val bin = BigInt(hash, 16).toString(2).take(3)
    val hashed_id = Integer.parseInt(bin, 2)
    println(hashed_id)








    /*println(md.digest("Vijay".getBytes("UTF-8")).map("%02x".format(_)).mkString)
    println(md.digest("Vijay".getBytes("UTF-8")).map("%02x".format(_)).mkString)
    println(md.digest("Singh".getBytes("UTF-8")).map("%02x".format(_)).mkString)*/
    //println(HashUtils.generateHash("2", 5, "SHA-1"))

    /*val hash = getHash("Saurabh",5)
    println(hash)*/

  }

  /*def getHash(key:String, m : Int): String = {

    val sha_instance = MessageDigest.getInstance("SHA-1")
    var sha_value:String =sha_instance.digest(key.getBytes).foldLeft("")((s:String, b: Byte) => s + Character.forDigit((b & 0xf0) >> 4, 16) +Character.forDigit(b & 0x0f, 16))
    var generated_hash:String =sha_value.substring(0,m)
    return generated_hash
  }*/
}
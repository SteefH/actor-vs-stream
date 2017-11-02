package avs.stream

import java.io.File

import scala.util.{Failure, Success, Try}

object WordCounterStreamApp extends App {

  Try {
    new File(args(0))
  } match {
    case Success(baseDir) => WordCounter.run(baseDir)
    case Failure(_) =>
      println("Use a base directory as the first command line argument")

  }

}

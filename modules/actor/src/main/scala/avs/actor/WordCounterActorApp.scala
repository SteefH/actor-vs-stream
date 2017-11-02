package avs.actor

import java.io.File

import scala.util.{Failure, Success, Try}

object WordCounterActorApp extends App {

  Try {
    new File(args(0))
  } match {
    case Success(baseDir) => WordCounter.run(baseDir)
    case Failure(_) =>
      println("Use a base directory as the first command line argument")

  }
}

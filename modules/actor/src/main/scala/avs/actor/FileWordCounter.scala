package avs.actor

import java.io.File

import akka.actor.Actor

import avs.util.FileLines

//import scala.concurrent.{ExecutionContext}

class FileWordCounter extends Actor {
  import FileWordCounter._
  import avs.util.AccumulationHelper._

//  implicit private val executionContext: ExecutionContext = context.dispatcher

  private val wordRe = "(-?\\w+-?)+".r

  private def countWordsInLine(line: String): Map[String, Long] =
    wordRe.findAllIn(line).foldLeft(Map.empty[String, Long]) {
      case (wordCountsForLine, word) =>
        wordCountsForLine.increaseCount(word.toLowerCase)
    }

  override def receive: Receive = {
    case Commands.ReadLines(file) =>
      val fileLines =
        FileLines
          .getLinesFromFile(file)
          .map(countWordsInLine)
          .reduceOption(_ sumCounts _)

      sender() ! fileLines.getOrElse(Map.empty)

  }

}

object FileWordCounter {
  object Commands {
    case class ReadLines(file: File)
  }
  case class Result(acc: Map[String, Long])
}

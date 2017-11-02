package avs.stream

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import avs.util.FileLines

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object WordCounter {
  import avs.util.AccumulationHelper._

  private def getFiles(baseDir: File): Source[File, NotUsed] = {

    Source[File](baseDir.listFiles.toStream).flatMapConcat[File, NotUsed] { f =>
      if (f.isDirectory) {
        getFiles(f)
      } else {
        Source.single(f)
      }
    }
  }

  private val getLines: Flow[File, String, NotUsed] =
    Flow
      .fromFunction(FileLines.getLinesFromFile)
      .mapConcat(_.toStream)

  private val splitIntoWords: Flow[String, String, NotUsed] = {
    val wordRe = "(-?\\w+-?)+".r
    Flow
      .fromFunction { s: String =>
        wordRe.findAllIn(s)
      }
      .mapConcat(_.toStream)
  }

  private def topTen(wordCounts: Map[String, Long]): Seq[(String, Long)] =
    wordCounts.toSeq sortBy {
      case (word, count) => (-count, word)
    } take 10

  def run(baseDir: File): Unit = {
    if (baseDir.isDirectory) {

      implicit val actorSystem: ActorSystem = ActorSystem("wordcounter")
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val ec: ExecutionContext = actorSystem.dispatcher

      getFiles(baseDir)
        .via(getLines)
        .via(splitIntoWords)
        .runFold(Map.empty[String, Long]) {
          case (acc, word) =>
            acc.increaseCount(word.toLowerCase)
        }
        .mapTo[Map[String, Long]]
        .onComplete { result =>
          result match {
            case Failure(ex) => ex.printStackTrace()
            case Success(s)  => println(topTen(s).mkString("\n"))
          }
          actorSystem.terminate()
        }
    } else {
      System.err.println(s"${baseDir.getPath} is not a directory")
    }
  }
}

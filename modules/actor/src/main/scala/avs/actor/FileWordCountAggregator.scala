package avs.actor

import java.io.File

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

class FileWordCountAggregator(implicit val askTimeout: Timeout) extends Actor {

  import avs.util.AccumulationHelper._

  implicit private val executionContext: ExecutionContext = context.dispatcher

  private val fileFinder = context.actorOf(Props(new FileFinder), "file-finder")

  override def receive: Receive = {
    case FileWordCountAggregator.Commands.Run(baseDir) =>
      val originalSender = sender()

      val allWordCounts = for {
        files <- findFiles(baseDir)
        wordCountsPerFile <- Future.sequence(files.map(countWordsInFile))
      } yield wordCountsPerFile.reduceOption(_ sumCounts _).getOrElse(Map.empty)

      allWordCounts onComplete { result =>
        originalSender ! result.getOrElse(Map.empty)
      }
  }

  private def countWordsInFile(file: File): Future[Map[String, Long]] = {
    val actor = context.actorOf(
      Props(new FileWordCounter),
      file.getAbsolutePath.replace("/", "-")
    )
    (actor ? FileWordCounter.Commands.ReadLines(file)).mapTo[Map[String, Long]]
  }

  private def findFiles(baseDir: File): Future[Seq[File]] =
    (fileFinder ? FileFinder.Commands.FindFiles(baseDir)).mapTo[Seq[File]]

}

object FileWordCountAggregator {
  object Commands {
    final case class Run(baseDir: File)
  }
}

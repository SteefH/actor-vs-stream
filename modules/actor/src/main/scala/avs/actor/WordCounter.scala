package avs.actor

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout

import scala.util.{Failure, Success}
import akka.pattern.ask

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object WordCounter {
  private def topTen(wordCounts: Map[String, Long]): Seq[(String, Long)] =
    wordCounts.toSeq sortBy {
      case (word, count) => (-count, word)
    } take 10

  def run(baseDir: File): Unit = {

    val actorSystem = ActorSystem("actor-wordcounter")
    implicit val executionContext: ExecutionContext =
      actorSystem.dispatcher

    implicit val runTimeout: Timeout = Timeout(60.seconds)

    val aggregator =
      actorSystem.actorOf(Props(new FileWordCountAggregator))
    (aggregator ? FileWordCountAggregator.Commands.Run(baseDir))
      .mapTo[Map[String, Long]]
      .onComplete { result =>
        result match {
          case Failure(ex)     => ex.printStackTrace()
          case Success(counts) => println(topTen(counts).mkString("\n"))
        }
        actorSystem.terminate()
      }
  }

}

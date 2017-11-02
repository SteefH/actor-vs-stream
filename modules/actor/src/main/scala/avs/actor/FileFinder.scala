package avs.actor

import java.io.File

import akka.actor.{Actor}

class FileFinder extends Actor {
  import FileFinder._

  private def findFiles(path: File): Seq[File] = {
    if (path.isFile) {
      Seq(path)
    } else {
      path.listFiles().flatMap(findFiles)
    }
  }
  override def receive = {
    case Commands.FindFiles(baseDir) =>
      sender ! findFiles(baseDir)
  }

}

object FileFinder {
  object Commands {
    final case class FindFiles(baseDir: File)
  }
}

package avs.util

import java.io.File
import java.nio.charset.CodingErrorAction

import scala.io.Codec

object FileLines {

  /**
    * stream lines from a file until an error occurs
    */
  def getLinesFromFile(file: File): Stream[String] = {

    implicit val codec: Codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    codec.decodingReplaceWith("?")

    scala.io.Source.fromFile(file).getLines.toStream
  }
}

package io.adjust.challenge.domain

import fs2.Pipe

object Normalize {

  def normalize[F[_]](state: (List[String], Int)): Pipe[F, String, List[String]] = { in =>
    in.scanChunks(state) { (state, chunk) =>
      chunk.mapAccumulate(state) { (headerState, elem) =>
        if (elem.isEmpty) {
          ((List[String](""), 0), List[String]())
        } else if (elem.charAt(0) != '#') {
          (
            (headerState._1, headerState._2 - 1),
            headerState._1 ++: lsplit(DataRecord.parsePath, elem).dropRight(1).map(el => el.strip)
          )
        } else {
          val header = lsplit(HeaderRecord.parsePath, elem).dropRight(1).map(el => el.strip)
          ((header, header(7).toInt), List[String]())
        }
      }
    }
  }

  def lsplit(pos: List[Int], str: String): List[String] = {
    val (rest, result) = pos.foldRight((str, List[String]())) {
      case (curr, (s, res)) =>
        val (rest, split) = s.splitAt(curr)
        (rest, split :: res)
    }
    rest :: result
  }

}

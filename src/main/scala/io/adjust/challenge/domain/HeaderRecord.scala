package io.adjust.challenge.domain

case class HeaderRecord(
    record: String,
    id: String,
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    reltime: Int,
    numlev: Int,
    p_src: String,
    np_src: String,
    lat: Int,
    long: Int
)

object HeaderRecord {
  val parsePath = List(1, 12, 17, 20, 23, 26, 31, 36, 45, 54, 62, 71)
}

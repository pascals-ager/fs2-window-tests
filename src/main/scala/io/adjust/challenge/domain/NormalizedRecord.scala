package io.adjust.challenge.domain

import cats.effect.IO
import io.adjust.challenge.utils.Transform

case class NormalizedRecord(
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
    long: Int,
    lvltypone: Int,
    lvltyptwo: Int,
    etime: Int,
    press: Int,
    pflag: String,
    gph: Int,
    zflag: String,
    temp: Int,
    tflag: String,
    rh: Int,
    dpdp: Int,
    wdir: Int,
    wspd: Int
)

object NormalizedRecord {
  implicit def TransformToNormalizedRecord: Transform[IO, List[String], NormalizedRecord] =
    in =>
      NormalizedRecord(
        in.head,
        in(1),
        in(2).toInt,
        in(3).toInt,
        in(4).toInt,
        in(5).toInt,
        in(6).toInt,
        in(7).toInt,
        in(8),
        in(9),
        in(10).toInt,
        in(11).toInt,
        in(12).toInt,
        in(13).toInt,
        in(14).toInt,
        in(15).toInt,
        in(16),
        in(17).toInt,
        in(18),
        in(19).toInt,
        in(20),
        in(21).toInt,
        in(22).toInt,
        in(23).toInt,
        in(24).toInt
      )

  val normalizedInsertSql =
    "insert into ADJUST_TBL (record, id, year, month, day, hour, reltime, numlev, p_src, np_src, lat, long, lvltypone, lvltyptwo, eltime, press, pflag, gph, zflag, temp, tflag, rh, dpdp, wdir, wspd) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
}

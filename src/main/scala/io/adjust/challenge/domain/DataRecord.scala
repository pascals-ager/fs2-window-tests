package io.adjust.challenge.domain

case class DataRecord(
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

object DataRecord {
  //20 11047    569 35278B -361B    5   426   338    75
  val parsePath = List(1, 2, 8, 15, 16, 21, 22, 27, 28, 33, 39, 45, 51)
}

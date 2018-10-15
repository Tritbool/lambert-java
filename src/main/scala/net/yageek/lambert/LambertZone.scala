package net.yageek.lambert

import net.yageek.lambert.ZoneCode._

import scala.math._

class LambertZone(val zone: Zone) {

  private val lambertZone: Short = setZone(zone)

  private val LAMBERT_N: Array[Double] = Array(0.7604059656, 0.7289686274, 0.6959127966, 0.6712679322, 0.7289686274, 0.7256077650)
  private val LAMBERT_C: Array[Double] = Array(11603796.98, 11745793.39, 11947992.52, 12136281.99, 11745793.39, 11754255.426)
  private val LAMBERT_XS: Array[Double] = Array(600000.0, 600000.0, 600000.0, 234.358, 600000.0, 700000.0)
  private val LAMBERT_YS: Array[Double] = Array(5657616.674, 6199695.768, 6791905.085, 7239161.542, 8199695.768, 12655612.050)

 val current_zone:()=>Short=()=>lambertZone

  def setZone(value: Zone): Short = {
    value match {
      case LambertI => 0
      case LambertII => 1
      case LambertIII => 2
      case LambertIV => 3
      case LambertIIExtended => 4
      case Lambert93 => 5
      case _ => -1
    }
  }

  val n: () => Double = () => LAMBERT_N(lambertZone)

  val c: () => Double = () => LAMBERT_C(lambertZone)

  val xs: () => Double = () => LAMBERT_XS(lambertZone)

  val ys: () => Double = () => LAMBERT_YS(lambertZone)

}
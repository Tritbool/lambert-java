package net.yageek.lambert

import scala.math.Pi

object Constants {
  val M_PI_2: Double = Pi / 2.0
  val DEFAULT_EPS: Double = 1e-10
  val E_CLARK_IGN: Double = 0.08248325676
  val E_WGS84: Double = 0.08181919106

  val A_CLARK_IGN: Double = 6378249.2

  val A_WGS84: Double = 6378137.0

  val LON_MERID_PARIS: Double = 0

  val LON_MERID_GREENWICH: Double = 0.04079234433

  val LON_MERID_IERS: Double = 3.0 * Pi / 180.0
}

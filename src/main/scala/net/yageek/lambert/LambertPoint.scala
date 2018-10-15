package net.yageek.lambert

import scala.math._

class LambertPoint(var x: Double, var y: Double, var z: Double) {

  def getX(): Double = {
    x
  }

  def setX(xi: Double) = {
    x = xi
  }

  def getY(): Double = {
    return y
  }

  def setY(yi: Double) = {
    y = yi
  }

  def getZ(): Double = {
    z
  }

  def setZ(zi: Double) = {
    z = zi
  }

  def translate(xi: Double, yi: Double, zi: Double) {

    x += xi
    y += yi
    z += zi
  }

  def toDegree(): LambertPoint = {
    x = x * 180 / Pi
    y = y * 180 / Pi
    z = z * 180 / Pi

    return this
  }
}

package com.futurice.testtoys

class RelativeRange(val value: Number, val range: Double) {
  override def equals(o: Any): Boolean = {
    var rv: Boolean = false
    if (o.isInstanceOf[String]) {
      try {
        val d: Double = (o.asInstanceOf[String]).toDouble
        rv = d >= value.doubleValue * (1 / range) && d <= value.doubleValue * range
      }
      catch {
        case e: NumberFormatException => {
        }
      }
    }
    return rv
  }

  override def toString: String = {
    return value.toString
  }
}
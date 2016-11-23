package com.futurice.testtoys

class Graph(var values: Array[Double]) {
  def max: Double = {
    var max: Double = 0
    for (d <- values) {
      max = Math.max(max, d)
    }
    return max
  }

  def min: Double = {
    var min: Double = Double.MaxValue
    for (d <- values) {
      min = Math.min(min, d)
    }
    return min
  }

  def toString(height: Int): String = {
    val rv: StringBuffer = new StringBuffer
    val max: Double = this.max
    val min: Double = this.min
    (0 until values.length) foreach { j =>
      rv.append("-")
    }
    rv.append('\n')
    var i: Int = 0
    while (i < height) {
      val low: Double = min + (height - 1 - i) * ((max - min) / height)
      val high: Double = min + (height - i) * ((max - min) / height)
      var j: Int = 0
      while (j < values.length) {
        if (values(j) >= low && values(j) <= high) {
          rv.append('o')
        }
        else if (values(j) >= high) {
          rv.append('|')
        }
        else {
          rv.append(' ')
        }
        j += 1;
      }
      rv.append("   " + f"${0.5 * (low + high)}%.3f\n")
      i += 1;
    }
    var j: Int = 0
    while (j < values.length) {
      rv.append("-")
      j += 1;
    }
    rv.append('\n')
    return rv.toString
  }
}
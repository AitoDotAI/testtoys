package com.futurice.testtoys

/**
  * Created by arau on 4.6.2016.
  */
object TestCommon {
  def matches(selection: Array[String], path: Array[String]): Boolean = {
    var i: Int = 0
    while (i < selection.length) {
      {
        if (selection(i).endsWith("*")) {
          if (!path(i).startsWith(selection(i).substring(0, selection(i).length - 1))) return false
        } else if (!(selection(i) == path(i))) {
          return false
        }
      }
      i += 1;
    }
    return true
  }
}

package com.futurice.testtoys

import java.io.IOException
import java.io.Reader

class PeekableReader(var r: Reader) {
  private val peeks: Array[Int] = new Array[Int](256)
  private var at: Int = 0
  private var n: Int = 0
  private var p: Int = 0

  @throws[IOException]
  def havePeek(i: Int) {
    while (i >= n) {
      {
        peeks((at + n) % peeks.length) = r.read
        n += 1
      }
    }
  }

  @throws[IOException]
  def close {
    r.close
  }

  @throws[IOException]
  def peek: Int = {
    havePeek(0)
    return peeks(at)
  }

  @throws[IOException]
  def peek(i: Int): Int = {
    if (i >= peeks.length) throw new RuntimeException("a peek too far, max=" + peeks.length)
    havePeek(i)
    return peeks((at + i) % peeks.length)
  }

  def pos: Int = {
    return p
  }

  @throws[IOException]
  def read: Int = {
    var rv: Int = -1
    if (n > 0) {
      rv = peeks(at)
      at = (at + 1) % peeks.length
      n -= 1
    }
    else {
      rv = r.read
    }
    p += 1
    return rv
  }

  override def toString: String = {
    val rv: StringBuffer = new StringBuffer
    var i: Int = 0
    var c : Int = 0
    while (i < n && {c = peek(i); c != -1}) {
      rv.append(c.toChar)
    }
    return rv.toString
  }
}
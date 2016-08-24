package com.futurice.testtoys

import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.ArrayList
import java.util.Iterator
import java.util.List

object TestTokenizer {
  def split(string: String): java.util.List[String] = {
    val t: TestTokenizer = new TestTokenizer(new StringReader(string))
    val rv = new java.util.ArrayList[String]
    while (t.hasNext) {
      rv.add(t.next)
    }
    return rv
  }
}

class TestTokenizer(val reader: Reader) extends Iterator[String] {
  prepareNext
  private var r = new PeekableReader(reader)
  private var n: String = null

  @throws[IOException]
  def close {
    if (r != null) {
      r.close
      r = null
    }
  }

  def prepareNext {
    if (n == null && r != null) {
      try {
        val b: StringBuffer = new StringBuffer
        if (r.peek == -1) {
          close
          return
        }
        else if (r.peek == ' ') {
          while (r.peek == ' ') {
            b.append(r.read.toChar)
          }
        }
        else if (r.peek == '\n') {
          b.append(r.read.toChar)
        }
        else if (Character.isDigit(r.peek)) {
          while (Character.isDigit(r.peek)) {
            b.append(r.read.toChar)
          }
          if (r.peek == '.' && Character.isDigit(r.peek(1))) b.append(r.read.toChar)
          while (Character.isDigit(r.peek)) {
            b.append(r.read.toChar)
          }
        }
        else if (Character.isAlphabetic(r.peek)) {
          while (Character.isAlphabetic(r.peek) || Character.isDigit(r.peek)) {
            {
              b.append(r.read.toChar)
            }
          }
        }
        else {
          b.append(r.read.toChar)
        }
        n = b.toString
      }
      catch {
        case e: IOException => {
          e.printStackTrace
          try {
            r.close
          }
          catch {
            case x: IOException => {
            }
          }
          r = null
        }
      }
    }
  }

  def hasNext: Boolean = {
    prepareNext
    return n != null
  }

  def peek: String = {
    prepareNext
    return n
  }

  def next: String = {
    prepareNext
    val rv: String = n
    n = null
    return rv
  }

  override def remove {
  }
}
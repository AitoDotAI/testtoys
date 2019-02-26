package com.futurice.testtoys

import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.ArrayList
import java.util.Iterator
import java.util.List

trait Tokenizer extends Iterator[String] {
  def peek : String
}

object Tokenizer {
  def splitWith(string:String, tokenizer:(StringReader)=> Iterator[String]) = {
    val t: Iterator[String] = tokenizer(new StringReader(string))
    val rv = new java.util.ArrayList[String]
    while (t.hasNext) {
      rv.add(t.next)
    }
    rv
  }
}

object TestTokenizer {

  def split(string: String): java.util.List[String] = {
    Tokenizer.splitWith(string, new TestTokenizer(_))
  }
}

class WhitespaceTokenizer(val reader:Reader) extends Tokenizer {

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
  def isWhitespace(c:Int): Boolean = {
    c == ' ' || c == '\t'
  }

  def prepareNext {
    if (n == null && r != null) {
      try {
        val b: StringBuffer = new StringBuffer
        if (r.peek == -1) {
          close
          return
        }
        else if (r.peek == '\n' || r.peek == '\r') {
          r.read
          b.append('\n')
        }
        else if (isWhitespace(r.peek)) {
          while (isWhitespace(r.peek)) {
            b.append(r.read.toChar)
          }
        }
        else {
          while (r.peek != -1 && !isWhitespace(r.peek) && r.peek != '\n' && r.peek != '\r') {
            b.append(r.read.toChar)
          }
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
  }}

class TestTokenizer(val reader: Reader) extends Tokenizer {
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
        else if (r.peek == '\n' || r.peek == '\r') {
          r.read
          b.append('\n')
        }
        else if (r.peek == '"') {
          while (r.peek != -1 && r.peek != '\n' && r.peek != '\r' && r.peek != '"') {
            b.append(r.read.toChar)
          }
        }
        else if (r.peek == '(') {
          while (r.peek != -1 && r.peek != '\n' && r.peek != '\r' && r.peek != ')') {
            b.append(r.read.toChar)
          }
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
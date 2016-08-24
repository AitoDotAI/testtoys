package com.futurice.testtoys

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.List

object TestTool {
  val INTERACTIVE: Int = 0
  val AUTOMATIC_FREEZE: Int = 1
  val NEVER_FREEZE: Int = 2
  val UNDEFINED: String = "__UNDEFINED__"
  val EOF: String = "__EOF__"
}

class TestTool @throws[IOException]
(val path: File, var report: PrintStream, var config: Int) {
  path.getParentFile.mkdirs
  if (path.exists) {
    for (f <- path.listFiles) {
      f.delete
    }
    path.delete
  }
  private var outFile: File = new File(path + "_out.txt")
  private var expFile: File = new File(path + "_exp.txt")
  private var filePath: File  = new File(path + "_out")
  private var out = new FileWriter(outFile)
  private var exp : BufferedReader =
    if (expFile.exists()) {
      new BufferedReader(new FileReader(expFile))
    } else {
      null
    }
  private var beginMs = System.currentTimeMillis

  private var expl: TestTokenizer = null
  private var errors: Int = 0
  private var lineOk: Boolean = true
  private var errorPos: Int = -1
  private var errorExp: String = null
  private var outline: StringBuffer = null
  private var expline: String = null

  prepareLine
  report.println("running " + path + "...\n")

  def this(path: String) {
    this(new File(path), System.out, if (System.getenv("TESTTOYS_NEVER_FREEZE") != null) TestTool.NEVER_FREEZE
    else if (System.getenv("TESTTOYS_ALWAYS_FREEZE") != null) TestTool.AUTOMATIC_FREEZE
    else TestTool.INTERACTIVE)
  }

  def this(path: String, report: PrintStream) {
    this(new File(path), report, if (System.getenv("TESTTOYS_NEVER_FREEZE") != null) TestTool.NEVER_FREEZE
    else if (System.getenv("TESTTOYS_ALWAYS_FREEZE") != null) TestTool.AUTOMATIC_FREEZE
    else TestTool.INTERACTIVE)
  }

  def this(path: String, config: Int) {
    this(new File(path), System.out, config)
  }

  def this(path: String, report: PrintStream, config: Int) {
    this(new File(path), report, config)
  }

  @throws[IOException]
  def close {
    out.close
    exp.close
  }

  def fileDir: File = {
    filePath.mkdir
    return filePath
  }

  def file(filename: String): File = {
    filePath.mkdir
    return new File(filePath, filename)
  }

  def dataFile(filename: String): File = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmm")
    return new File(filePath + "_" + filename + "_" + sdf.format(new Date))
  }

  @throws[IOException]
  def prepareLine {
    outline = new StringBuffer
    if (exp != null) {
      expline = exp.readLine
      if (expline != null) {
        expl = new TestTokenizer(new StringReader(expline + "\n"))
      }
    }
    lineOk = true
    errorPos = -1
    errorExp = null
  }

  def parse(s: String): java.util.List[String] = {
    return TestTokenizer.split(s)
  }

  def isExp: Boolean = {
    return expl != null
  }

  @throws[IOException]
  def read: String = {
    if (expl == null) return (if (exp == null) TestTool.UNDEFINED
    else TestTool.EOF)
    return expl.next
  }

  @throws[IOException]
  def peek: String = {
    if (expl == null) return (if (exp == null) TestTool.UNDEFINED
    else TestTool.EOF)
    return expl.peek
  }

  @throws[IOException]
  def peekDouble: Option[Double] = {
    try {
      return Some(peek.toDouble)
    }
    catch {
      case e: NumberFormatException => {
        return None
      }
      case e: NullPointerException => {
        return None
      }
    }
  }

  @throws[IOException]
  def peekLong: Option[Long] = {
    try {
      return Some(peek.toLong)
    } catch {
      case e: NumberFormatException => {
        return None
      }
    }
  }

  private[testtoys] def printError(tag: String, outline: String, expline: String) {
    report.print(tag + outline.toString)
    if (expline != null) {
      val pad: Int = 50 - 2 - outline.length
      var i: Int = 0
      while (i < pad) {
        report.print(' ')
        i += 1;
      }
      report.println("|" + expline.toString)
    }
    else {
      report.println
    }
  }

  @throws[IOException]
  private[testtoys] def lineDone {
    out.flush
    if (!lineOk) {
      errors += 1
      printError("! ", outline.toString, expline)
    }
    else {
      report.println("  " + outline.toString)
    }
    prepareLine
  }

  def test(t: Any, e: String): Boolean = {
    if (e eq TestTool.UNDEFINED) return true
    if (e == null) return false
    return t == e
  }

  @throws[IOException]
  def ignoreToken(t: Any) {
    read
    out.write(t.toString)
    if (t == "\n") {
      lineDone
    }
    else {
      outline.append(t)
    }
  }

  @throws[IOException]
  def feedToken(t: Any) {
    val e: String = read
    if (!test(t, e)) {
      lineOk = false
      if (errorPos == -1) {
        errorPos = outline.length
        errorExp = e
      }
    }
    out.write(t.toString)
    if (t == "\n") {
      lineDone
    }
    else {
      outline.append(t)
    }
  }

  def pos: Int = {
    return outline.length
  }

  @throws[IOException]
  def t(s: String) {
    import scala.collection.JavaConversions._
    for (t <- parse(s)) {
      feedToken(t)
    }
  }

  @throws[IOException]
  def i(t: Any) {
    ignoreToken(t)
  }

  @throws[IOException]
  def i(s: String) {
    import scala.collection.JavaConversions._
    for (t <- parse(s)) {
      ignoreToken(t)
    }
  }

  @throws[IOException]
  def igf(f: String, args: Any*) {
    i(String.format(f, args))
  }

  @throws[IOException]
  def iln(s: String) {
    i(s)
    ignoreToken("\n")
  }

  @throws[IOException]
  def ifln(f: String, args: Any*) {
    igf(f, args)
    ignoreToken("\n")
  }

  @throws[IOException]
  def tln(s: String) {
    t(s)
    feedToken("\n")
  }

  @throws[IOException]
  def tf(s: String, args: Any*) {
    t(s.format(args:_*))
  }

  @throws[IOException]
  def tfln(s: String, args: Any*) {
    tf(s, args:_*)
    feedToken("\n")
  }

  @throws[IOException]
  def tln {
    feedToken("\n")
  }

  @throws[IOException]
  def t(file: File) {
    val r: BufferedReader = new BufferedReader(new FileReader(file))
    var l: String = null
    while ({l = r.readLine; l != null}) {
      tln(l)
    }
    r.close
  }

  @throws[IOException]
  def done: Boolean = {
    var ok: Boolean = errors == 0
    val ms: Long = System.currentTimeMillis - beginMs
    if (outline.length > 0) {
      lineDone
    }
    if (expline != null) {
      errors += 1
      printError("EOF", "", expline)
    }
    out.close
    if (exp != null) exp.close
    report.println
    report.print(ms + " ms. ")
    if (errors > 0) {
      report.print(errors + " errors! ")
    }
    if (exp == null || errors > 0) {
      var freeze: Boolean = (config == TestTool.AUTOMATIC_FREEZE)
      if (config == TestTool.INTERACTIVE) {
        var cont = true
        while (cont) {
          System.out.print("[d]iff, [c]ontinue or [f]reeze?")
          val line: String = new BufferedReader(new InputStreamReader(System.in)).readLine
          if (line == "d") {
            val params: Array[String] = new Array[String](3)
            params(0) = "/usr/bin/meld"
            params(1) = expFile.getAbsolutePath
            params(2) = outFile.getAbsolutePath
            Runtime.getRuntime.exec(params)
          } else if (line == "f") {
            freeze = true
            cont = false
          } else if (line == "c") {
            freeze = false
            cont = false
          }
        }
      }
      if (freeze) {
        outFile.renameTo(expFile)
        report.println("frozen.")
        ok = true
      }
    }
    report.println
    return ok
  }
}
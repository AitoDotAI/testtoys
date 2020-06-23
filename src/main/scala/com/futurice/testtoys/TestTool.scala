package com.futurice.testtoys

import java.io._
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.Date
import java.util.List

import scala.language.experimental.macros
import scala.reflect.macros.Context

object TestTool {
  val INTERACTIVE: Int = 0
  val AUTOMATIC_FREEZE: Int = 1
  val NEVER_FREEZE: Int = 2
  val FAIL_ON_ERROR: Int = 3
  val UNDEFINED: String = "__UNDEFINED__"
  val EOF: String = "__EOF__"

  def ms[T](f: =>T) = {
    val before = System.currentTimeMillis()
    val rv = f
    (System.currentTimeMillis-before, rv)
  }

  def time[T](f: =>T, temporalUnit: TemporalUnit) = {
    val before = Instant.now()
    val rv = f
    val after = Instant.now()

    (before.until(after, temporalUnit), rv)
  }

  def us[T](f: =>T) = time(f, ChronoUnit.MICROS)

  sealed trait FileFormat
  case class TextFormat(charset: Charset, lines: Long) extends FileFormat
  case object BinaryFormat extends FileFormat

  def parseTestToysMode(): Int = {
    if (System.getenv("TESTTOYS_FAIL_ON_ERROR") != null) TestTool.FAIL_ON_ERROR
    else if (System.getenv("TESTTOYS_NEVER_FREEZE") != null) TestTool.NEVER_FREEZE
    else if (System.getenv("TESTTOYS_ALWAYS_FREEZE") != null) TestTool.AUTOMATIC_FREEZE
    else TestTool.INTERACTIVE
  }

  def diffTool(): Option[Seq[String]] = {
    Option(System.getenv("TESTTOYS_DIFF_TOOL")) match {
      case Some("idea") => Some(Seq("idea", "diff"))
      case _ => None
    }
  }
  def assertImpl(c: Context)(t:c.Expr[TestTool], cond: c.Expr[Boolean]): c.Expr[Unit] = {
    import c.universe._
    val expr = cond.tree.toString()
    val pos = c.macroApplication.pos
    val exprStr = c.Expr[String](Literal(Constant(expr)))
    val posStr = c.Expr[String](Literal(Constant(s"${pos.source.file.name + ":" + pos.line}")))

    reify({
      t.splice.t("asserting '" + exprStr.splice + "'..").assert(cond.splice).iln(" [" + posStr.splice + "]")
    })
  }

  def assert(t:TestTool, cond:Boolean) = macro assertImpl
}

class TestTool @throws[IOException]
(val path: File, var report: PrintStream, var config: Int) {
  private val outFile: File = new File(path + "_out.txt")
  private val expFile: File = new File(path + "_exp.txt")
  private val filePath: File  = new File(path + "_out")

  private val maxRetriesCount = 20

  import TestTool.{ FileFormat, TextFormat, BinaryFormat }

  path.getParentFile.mkdirs
  if (filePath.exists) {
    TestCommon.rmdir(filePath)
  }

  private val out = new FileWriter(outFile)
  private val exp : BufferedReader =
    if (expFile.exists()) {
      new BufferedReader(new FileReader(expFile))
    } else {
      null
    }
  private val beginMs = System.currentTimeMillis

  private var expl: Tokenizer = null
  private var errors: Int = 0
  private var lineOk: Boolean = true
  private var errorPos: Int = -1
  private var outline: StringBuffer = null
  private var expline: String = null

  prepareLine
  report.println("running " + path + "...\n")


  def this(path: String) {
    this(new File(path), System.out, TestTool.parseTestToysMode())
  }

  def this(path: String, report: PrintStream) {
    this(new File(path), report, TestTool.parseTestToysMode())
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
        expl = tokenizer(new StringReader(expline + "\n"))
      }
    }
    lineOk = true
    errorPos = -1
  }

  def tokenizer(reader:Reader) = {
    new TestTokenizer(reader)
  }

  def parse(s: String): java.util.List[String] = {
    return Tokenizer.splitWith(s, tokenizer(_))
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
    (t, e) match {
      case (_,  TestTool.UNDEFINED) => return true
      case (_,  null) => return false
      case (t:Double,e) => t - e.toDouble < 0.0000001
      case (t:String, e:String) => t == e
      case (t, e) => t.toString == e
    }
  }

  @throws[IOException]
  def ignoreToken(t: Any): TestTool = {
    read
    out.write(t.toString)
    if (t == "\n") {
      lineDone
    }
    else {
      outline.append(t)
    }
    this
  }
  def fail : TestTool = {
    lineOk = false
    if (errorPos == -1) {
      errorPos = outline.length
    }
    this
  }

  @throws[IOException]
  def feedToken(t: Any): TestTool =  {
    val e: String = read
    if (!test(t, e)) fail
    out.write(t.toString)
    if (t == "\n") {
      lineDone
    }
    else {
      outline.append(t)
    }
    this
  }

  def pos: Int = {
    return outline.length
  }

  def assert(cond:Boolean) = {
    if (!cond) {
      e("FAILED!")
    } else {
      i("ok")
    }
  }

  def assertln(cond:Boolean) = {
    assert(cond)
    iln
  }

  /** error: always fail this line  */
  def e(str:String) = {
    fail
    i(str)
  }

  def error(str:String) = e(str)

  /** error: always fail this line  */
  def eln(str:String) = {
    fail
    iln(str)
  }

  def errorln(str:String) = eln(str)

  /**
    * Print/ignore a token
    */
  @throws[IOException]
  def i(t: Any) : TestTool = {
    ignoreToken(t)
  }

  /**
    * Print a comment / ignore this line in testing
    */
  @throws[IOException]
  def i(s: String) : TestTool = {
    import scala.collection.JavaConversions._
    for (t <- parse(s)) {
      ignoreToken(t)
    }
    this
  }

  @throws[IOException]
  def igf(f: String, args: Any*): TestTool =  {
    i(String.format(f, args))
  }

  @throws[IOException]
  def iln(s: String) : TestTool = {
    i(s)
    ignoreToken("\n")
  }

  @throws[IOException]
  def iln : TestTool = {
    ignoreToken("\n")
  }

  @throws[IOException]
  def ifln(f: String, args: Any*): TestTool =  {
    igf(f, args)
    ignoreToken("\n")
  }


  /**
    * Test this line
    */
  @throws[IOException]
  def t(s: String) : TestTool = {
    import scala.collection.JavaConversions._
    for (t <- parse(s)) {
      feedToken(t)
    }
    this
  }

  @throws[IOException]
  def tln(s: String) : TestTool = {
    t(s)
    feedToken("\n")
  }

  @throws[IOException]
  def tf(s: String, args: Any*): TestTool =  {
    t(s.format(args:_*))
  }

  @throws[IOException]
  def tfln(s: String, args: Any*) : TestTool = {
    tf(s, args:_*)
    feedToken("\n")
  }

  @throws[IOException]
  def tln : TestTool = {
    feedToken("\n")
  }

  @throws[IOException]
  def t(file: File) : TestTool = {
     t(file, None, -1)
    this
  }

  @throws[IOException]
  def t(file: File, lines:Int): TestTool =  {
     t(file, None, lines)
    this
  }

  @throws[IOException]
  def t(file: File, charset:Option[Charset], lines:Int): TestTool =  {
    t(file, TextFormat(charset getOrElse Charset.defaultCharset, lines))
    this
  }

  @throws[IOException]
  def t(file: File, charset:Charset, lines:Int): TestTool =  {
    t(file, TextFormat(charset, lines))
    this
  }

  @throws[IOException]
  def t(file: File, charset:Charset): TestTool =  {
    t(file, TextFormat(charset, -1))
    this
  }

  @throws[IOException]
  def tText(file: File, format: TextFormat): TestTool =  {
    val r =
      new BufferedReader(
        new InputStreamReader(new FileInputStream(file), format.charset))
    try { 
      var l: String = null
      var line = 0
      while ({l = r.readLine; l != null && line != format.lines}) {
        tln(l)
        line += 1
      }
    } finally {
      r.close
    }
    this
  }

  @throws[IOException]
  def tBinary(file: File): TestTool =  {
    // Displays only 32 bytes per line in hexadecimal, not for large files!
    val buf = new Array[Byte](32)
    val is = new BufferedInputStream(new FileInputStream(file))
    try {
      var n = 0
      while ({ n = is.read(buf); n > 0}) {
        val line = if (n < buf.length) buf.take(n) else buf
        line.iterator.grouped(4).grouped(2).zipWithIndex.foreach {
          case (words, i) =>
            if (i > 0) t("  ")
            words.zipWithIndex.foreach { case (word, j) =>
              if (j > 0) t(s" ")
              t(word.map("%02x".format(_)).mkString)
            }
        }
        tln
      }
    } finally {
      is.close
    }
    this
  }

  @throws[IOException]
  def t(file: File, format: FileFormat): TestTool =  {
    format match {
      case f: TextFormat => tText(file, f)
      case BinaryFormat => tBinary(file)
    }
    this
  }

  def tLong(time:Long, unit:String = "", relRange:Double = 10.0): TestTool = {
    val v = peekLong
    val postfix = unit match {
      case "" => ""
      case v => " " + v
    }
    i(f"$time$postfix ")
    v match {
      case Some(old) =>
        if (relRange.isPosInfinity||
	    Math.abs(Math.log(time/old.toDouble)) < Math.log(relRange)||old==0||(old==1&&v==0)) {
          i(f"(was $old$postfix)")
        } else {
          t(f"(${((time*100)/old.toDouble).toInt}%% of old $old$postfix)")
        }
      case None =>
        peek match {
          case "(" =>
            read
            while (peek != ")" && peek != "\n") read
          case _ =>
        }
    }
    this
  }

  def tLongLn(time:Long, unit:String = "", relRange:Double = 10.0): TestTool = {
    tLong(time, unit, relRange)
    ignoreToken("\n")
  }

  def tUsPerOpLn[T](opCount:Int, unit:String ="unit", relRange : Double = 10.0)(f : => T) : T = {
    val (ms, rv) =
      TestTool.ms(f)
    tDoubleLn(1000*ms/opCount.toDouble, f"us/$unit", relRange)
    rv
  }

  def iUsPerOpLn[T](opCount:Int, unit:String ="unit")(f : => T) : T = {
    tUsPerOpLn(opCount, unit, Double.PositiveInfinity)(f)
  }


  def tDouble(time:Double, unit:String = "", relRange:Double = 10.0) : TestTool = {
    val v = peekDouble
    i(f"$time%.3f $unit ")
    v match {
      case Some(old) =>
        if (relRange.isPosInfinity
	    ||Math.abs(Math.log(time/old.toDouble)) < Math.log(relRange)||old==0) {
          i(f"(was $old%.3f $unit)")
        } else {
          t(f"(${((time*100)/old.toDouble).toInt}%% of old $old%.3f $unit)")
        }
      case None =>	
    }
    this
  }

  def tDoubleLn(time:Double, unit:String = "", relRange:Double = 10.0): TestTool = {
    tDouble(time, unit, relRange)
    ignoreToken("\n")
  }

  def tMs[T](relRange:Double, f : => T) : T = {
    val (m, rv) = TestTool.ms(f)
    tLong(m, "ms", relRange)
    rv
  }
  // allow the value to be twice as big or half as small
  def tMs[T](f: => T) : T =
    tMs(10, f)

  def tMsLn[T](f: => T) : T = {
    val rv = tMs(10, f)
    i("\n")
    rv
  }

  def iMs[T](f: =>T) : T =
    tMs(Double.PositiveInfinity, f)

  def iMsLn[T](f: =>T) : T = {
    val rv = tMs(Double.PositiveInfinity, f)
    i("\n")
    rv
  }

  def tTime[T](temporalUnit: TemporalUnit, relRange:Double)(f : => T) : T = {
    val (t, rv) = TestTool.time(f, temporalUnit)
    tLong(t, temporalUnit.toString, relRange)
    rv
  }


  // allow the value to be twice as big or half as small
  def tTime[T](temporalUnit: TemporalUnit)(f: => T) : T =
    tTime(temporalUnit, 10)(f)

  def tTimeLn[T](temporalUnit: TemporalUnit)(f: => T)  : T = {
    val rv = tTime(temporalUnit, 10)(f)
    i("\n")
    rv
  }
  def iTime[T](temporalUnit: TemporalUnit)(f: =>T) : T =
    tTime(temporalUnit, Double.PositiveInfinity)(f)

  def iTimeLn[T](temporalUnit: TemporalUnit)(f: =>T) : T = {
    val rv = tTime(temporalUnit, Double.PositiveInfinity)(f)
    i("\n")
    rv
  }

  def iUsLn[T](f : => T) = iTimeLn(ChronoUnit.MICROS)(f)
  def tUsLn[T](f : => T) = tTimeLn(ChronoUnit.MICROS)(f)

  // Extra action is of form (ok, expFile, outFile => (freeze, continue)

  type Action = (Boolean, File, File)=>(Boolean, Boolean)
  type ActionEntry = (String, String, Action)

  def diffToolAction(toolCmd:String = "meld",
                     action:String = "[d]iff",
                     command:String = "d") = {
    (action, command,
      (_:Boolean, expFile:File, outFile:File) => {
        (action, command)

        val params: Seq[String] = TestTool.diffTool() match {
          case Some(s) => s
          case None => Seq(toolCmd)
        }

        val exec = params :+
          expFile.getAbsolutePath :+
          outFile.getAbsolutePath

        Runtime.getRuntime.exec(exec.toArray)
        (false, true)
    })
  }


  @throws[IOException]
  def done(extraActions:Seq[ActionEntry] = Seq(diffToolAction())): Boolean = {
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
      if (config == TestTool.FAIL_ON_ERROR) {
        throw new RuntimeException(s"Test failed in TestTool. TestTool config set to fail on errors (expFile: ${expFile.getAbsolutePath} => outFile: ${outFile.getAbsolutePath})")
      } else if (config == TestTool.INTERACTIVE) {
        var cont = true
        var counter = 0
        while (cont) {
          if (counter > maxRetriesCount) {
            throw new IllegalStateException(s"Exceeded the max retries count '${maxRetriesCount}' in ${this.getClass.toString}")
          }

          System.out.print((extraActions.map(_._1) ++ Seq("[c]ontinue")).mkString(", ") +  " or [f]reeze?")
          val line: String = new BufferedReader(new InputStreamReader(System.in)).readLine
          if (line == "f") {
            freeze = true
            cont = false
          } else if (line == "c") {
            freeze = false
            cont = false
          } else {
            extraActions.map(e => (e._2, e._3)).toMap.get(line) match {
              case None => {
                counter += 1
              }
              case Some(action) =>
                val (f, c) = action(ok, expFile, outFile)
                freeze = f
                cont = c
            }
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

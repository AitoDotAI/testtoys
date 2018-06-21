package com.futurice.testtoys

import org.junit.Assert.assertTrue
import java.io.File
import java.io.FileWriter
import java.io.IOException
import org.junit.Test

class TestToolTest extends TestSuite("testtool") {

  test("basics") { t =>
    t.tln("line printed with tln(string) needs to remain as it is")
    t.tfln("tfln(string, format) is for formatting things like these %02d %02d %.1f", 3, 31, 3.14)
    t.iln("iln(string) prints changing content without breaking test. time is " + System.currentTimeMillis)
    t.tln("")
    t.tln("the nice thing in the test tool is that it's self documenting")
  }

  test("files") { t =>
    t.tln("file(filename) can be used to get names for files in test directory")
    t.tln("each test case has its very own directory, where file are written.")
    t.tln
    t.tln("let's create a file")
    val f: File = t.file("test.txt")
    t.tln("this file has name " + f.getName + "")
    t.iln("path is " + f.getAbsolutePath)
    t.tln("path may vary, so we printed it with iln(str)")
    t.tln
    t.tln("let's write content in the file.")
    val w: FileWriter = new FileWriter(f)
    try {
      w.write("lorem ipsum\nfoo bar\n")
    } finally {
      w.close
    }
    t.tln("you can easily feed the file back to test tool for testing.")
    t.tln("file content is this:\n")
    t.t(f)
    t.tln
    t.tln("if the content changes, the test will break.")
  }

  test("binary-files") { t =>
    val f: File = t.file("test.bin")
    val os = new java.io.FileOutputStream(f)
    try {
      os.write(Array[Byte](
        -12, 0, -7, -6, -34, 105, -42, -101, -107, -17, -30, -97, -5, 97, -118,
        -49, -33, -94, -71, -12, -73, -1, -45, -54, -104, 0, 0, 106, 89, 114,
        -2, 43, 127, -62, 41, 101, -2, 90, 40, -109, -7, 98, -111, -17, -19,
        -122, 43, 63, -4, 0, 0, 4, 0, 0, 0, 16, 0, 0, 0, 1, 0, 0, 0, 74, 1, 27,
        0, 5, 0, 0, 0, 1, 0, 0, 0, 82, 1, 40, 0, 3, 0, 0, 0, 1, 0, 2, 0, 0,
        -121, 105, 0, 4, 0, 0, 0, 1, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 0, 72, 0,
        0, 0, 1, 0, 0, 67, -104, 49, 24, -96, 7, -94, -128, 87, -23, -128, -96,
        1, 50, 1, -123, -96, 2, 10, -106, -35, -60, 5, -93, 67, -10, 1, -2,
        -88, 36, -64, 54, 120, -41, 67, -94, -112, 18, -96, -111, 31, -1, -39))
    } finally {
      os.close
    }

    t.tln("tBinary(file) can be used to include binary files in the output of")
    t.tln("a test. When included it is shown in base-16 and looks like this:")
    t.tln
    t.tBinary(f)
    t.tln
    t.tln("The output is not very compact and should be used for small binary")
    t.tln("files only. The diff is mainly useful to check whether the binary")
    t.tln("format of a file has changed but in simple cases it is possible to")
    t.tln("figure out what changed exactly.")
  }

  test("peeks") { t =>
    t.tln("for performance testing we can use peekLong() facility")
    t.tln("let's measure how long sleeping 100ms takes")
    val before: Long = System.currentTimeMillis
    Thread.sleep(100)
    val timeMs: Long = System.currentTimeMillis - before
    t.t("sleep(100) took ")
    val o = t.peekLong
    t.i(timeMs)
    t.tln(" ms")
    o match {
      case Some(old) =>
        t.iln("old result was " + old + " ms")
        val delta: Long = old - timeMs
        if (delta > 5) {
          t.tln("we were " + delta + " ms faster!")
        }
        else if (delta < -5) {
          t.tln("we were " + -delta + " ms slower!")
        }
        else {
          t.iln("ok, no significant difference")
        }
      case None =>
       t.tln("no old result\n")
    }
    t.tln
    t.tln("using peeks is unfortunately verbose in code,")
    t.tln("but this is mostly because comparisons, which ")
    t.tln("tend to be case specific any way.")
  }

  test("feed") { t =>
    t.tln("feedToken(object) can be used to do more specific comparisons, e.g:\n")
    t.tln("t.tLong(100, 1.10) matches numeric range 91-110")
    t.t("feeding ")
    var old = t.peekLong
    t.tLongLn(100, "",  1.1)
    t.iln("  compared to " + old.getOrElse("(none)") + "\n")
    t.tln("t.tLong(100, 2.) matches numeric range 50-200")
    t.t("feeding ")
    old = t.peekLong
    t.tLongLn(100, "",  2.0)
    t.iln("  compared to " + old.getOrElse("(none)"))
    t.tln("\nthe comparison object need to fullfil equals() so that it reads string")
    t.tln("and toString() so that it can be printed in output")
  }
}

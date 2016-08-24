package com.futurice.testtoys

import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayList
import java.util.List

import scala.collection.mutable.ArrayBuffer

class TestSuite(val name:String) {
  type TestEntry = (String, (TestTool) => Unit)

  private var tests = new ArrayBuffer[TestEntry]

  def test(name: String) (test: (TestTool) => Unit) {
    tests += name -> test
  }

  @throws[IOException]
  def run(selection: String, rootPath:File, config: Int) = {
    val selected: Seq[TestEntry] =
      if (selection.isEmpty) {
        tests
      } else {
        selection.split(",").flatMap { n =>
          tests.find(_._1 == n)
        }
      }

    val path = new File(rootPath, name)

    selected.map { e =>
      val t: TestTool = new TestTool(new File(path, e._1), System.out, config)
      try {
        e._2(t)
      } catch {
        case x: Exception => {
          val w: StringWriter = new StringWriter
          x.printStackTrace(new PrintWriter(w))
          t.t(w.toString)
          w.close
        }
      }
      t.done
    }.fold(true)(_ && _)
  }
}
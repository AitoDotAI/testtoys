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

  val Fail = 0
  val Ok = 1
  val Quit = 2
  type Result = Int

  private var tests = new ArrayBuffer[TestEntry]

  def test(name: String) (test: (TestTool) => Unit) {
    tests += name -> test
  }

  @throws[IOException]
  def run(selection: String, rootPath:File, config: Int) = {
    val path = new File(rootPath, name)

    def runTest(e:TestEntry ) = {
      val tt: TestTool = new TestTool(new File(path, e._1), System.out, config)

      e._2(tt)

      var quit = false
      var res = tt.done(Seq(tt.diffToolAction(),("[q]uit", "q", (_, _, _) => {
        quit = true
        (false, false)
      })))
      (quit, res) match {
        case (true, _) => Quit
        case (false, true) => Ok
        case (false, false) => Fail
      }
    }

    def testOps =
      tests.map( t =>
        (t._1, () => runTest(t)))


    def ops : Seq[(String, () => Int)] = {
      testOps ++
        Array(("-l", { () =>
          testOps.foreach { t =>
            System.out.println(t._1)
          }
          Ok
        }))
    }
    val selected =
      if (selection.size == 0) {
        testOps
      } else {
        ops.filter{ case (name, op) => name.matches(selection) }
      }

    val s = selected.iterator
    var cont = true
    var ok = true
    while (cont && s.hasNext) {
      s.next._2() match {
        case Ok =>
        case Fail => ok = false
        case Quit => ok = false; cont = false
      }
    }
    ok
  }
}
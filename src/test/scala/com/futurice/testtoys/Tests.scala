package com.futurice.testtoys

import java.util.Locale

object Tests {
  def main(args:Array[String]): Unit = {
    Locale.setDefault(Locale.ENGLISH)
    TestRunner(
      "testio",
      Seq( new ExampleTest, new PathTest, new TestToolTest))
      .exec(args)
  }
}

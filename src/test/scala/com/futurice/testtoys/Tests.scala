package com.futurice.testtoys

object Tests {
  def main(args:Array[String]): Unit = {
    TestRunner(
      "testio",
      Seq( new ExampleTest, new TestToolTest))
      .exec(args)
  }
}
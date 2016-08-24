package com.futurice.testtoys

import org.junit.Assert.assertTrue
import java.io.IOException
import org.junit.Test

class ExampleTest extends TestSuite("example") {
  test("plus") { t =>
    t.tln("testing plus operation")
    t.tln("  1+1=" + (1 + 1))
  }
}
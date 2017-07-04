package com.futurice.testtoys

/**
  * Created by arau on 12.6.2017.
  */
class PathTest extends TestSuite("path/to/place") {
  test("first") { t =>
    t.tln("  this is first test")
  }
  test("second") { t =>
    t.tln("  this is second test")
  }
}

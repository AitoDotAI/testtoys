package com.futurice.testtoys;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;


import com.futurice.testtoys.RelativeRange;
import com.futurice.testtoys.TestTool;

public class TestToolTest {
	
	@Test
	public void testBasics() throws IOException  {
		TestTool t = new TestTool("testio/testtool/basics"); 
		
		t.tln("line printed with tln(string) needs to remain as it is"); 
		t.tfln("tfln(string, format) is for formatting things like these %02d %02d %.1f", 3, 31, 3.14 ); 
		t.iln("iln(string) prints changing content without breaking test. time is " + System.currentTimeMillis()); 
	
		t.tln(""); 
		t.tln("the nice thing in the test tool is that it's self documenting"); 

		assertTrue(t.done());
	}

	@Test
	public void testFiles() throws IOException  {
		TestTool t = new TestTool("testio/testtool/files"); 

		t.tln("file(filename) can be used to get names for files in test directory");
		t.tln("each test case has its very own directory, where file are written.");
		t.tln();
		t.tln("let's create a file"); 
		File f = t.file("test.txt");
		t.tln("this file has name " + f.getName() + "");
		t.iln("path is " + f.getAbsolutePath()); 
		t.tln("path may vary, so we printed it with iln(str)");
		t.tln(); 

		t.tln("let's write content in the file."); 
		
		FileWriter w = new FileWriter(f); 
		try {
			w.write("lorem ipsum\nfoo bar\n"); 
		} finally {
			w.close(); 
		}
		t.tln("you can easily feed the file back to test tool for testing."); 
		t.tln("file content is this:\n"); 
		t.t(f);
		t.tln(); 
		t.tln("if the content changes, the test will break."); 

		assertTrue(t.done());
	}


	@Test
	public void testPeeks() throws Exception  {
		TestTool t = new TestTool("testio/testtool/peeks"); 

		t.tln("for performance testing we can use peekLong() facility");
		t.tln("let's measure how long sleeping 100ms takes");
		long before = System.currentTimeMillis();
		Thread.sleep(100);
		long timeMs = System.currentTimeMillis() - before;
		t.t("sleep(100) took ");
		Long old = t.peekLong();
		t.i(timeMs); 
		t.tln(" ms");
		if (old != null) {
			t.iln("old result was " + old + " ms"); 
			long delta = old - timeMs; 
			if (delta > 5) {
				t.tln("we were " + delta + " ms faster!"); 
			} else if (delta < -5) {
				t.tln("we were " + -delta + " ms slower!"); 
			} else {
				t.iln("ok, no significant difference"); 
			}
		} else {
			t.tln("no old result\n");
		}
		t.tln(); 
		t.tln("using peeks is unfortunately verbose in code,");
		t.tln("but this is mostly because comparisons, which ");
		t.tln("tend to be case specific any way.");
		
		assertTrue(t.done());
	}

	@Test
	public void testFeed() throws IOException {
		TestTool t = new TestTool("testio/testtool/feed"); 
		
		t.tln("feedToken(object) can be used to do more specific comparisons, e.g:\n"); 
		t.tln("t.feedToken(RelativeRange(100, 1.10)) matches numeric range 91-110");
		t.t("feeding ");
		Long old = t.peekLong();
		t.feedToken(new RelativeRange(100, 1.1)); 
		t.iln("\n  compared to " + old + "\n"); 

		t.tln("t.feedToken(RelativeRange(100, 2.)) matches numeric range 50-200");
		t.t("feeding ");
		old = t.peekLong();
		t.feedToken(new RelativeRange(100, 2.)); 
		t.iln("\n  compared to " + old); 

		t.tln("\nthe comparison object need to fullfil equals() so that it reads string");
		t.tln("and toString() so that it can be printed in output");

		assertTrue(t.done());
	}

}

package com.futurice.testtoys;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.futurice.testtoys.TestTool;

public class ExampleTest {	
	@Test
	public void testPlus() throws IOException  {
		TestTool t = new TestTool("testio/example/plus"); 

		t.tln("testing plus operation"); 
		t.tln("  1+1=" + (1+1));
  
        assertTrue(t.done());
	}
}

package com.futurice.testtoys;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.IOException;

public class ExampleTest {	
	@Test
	public void testPlus() throws IOException  {
		TestTool t = new TestTool("testio/example/plus"); 

		t.tln("testing plus operation"); 
		t.tln("  1+1=" + (1+1));
  
        assertTrue(t.done());
	}
}

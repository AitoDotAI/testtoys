package com.futurice.testtoys;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestTokenizer implements Iterator<String> {
	
	public static List<String> split(String string) {
		TestTokenizer t = new TestTokenizer(new StringReader(string)); 
		List<String> rv = new ArrayList<String>(); 
		while (t.hasNext()) {
			rv.add(t.next()); 
		}
		return rv; 
	}

	private PeekableReader r; 
	
	private String next; 
	
	public TestTokenizer(Reader r) {
		this.r = new PeekableReader(r);
		prepareNext();
	}
	
	public void close() throws IOException {
		if (r != null) {
			r.close(); 
			r = null; 
		}
	}
	
	public void prepareNext() {
		if (next == null && r != null) {
			try {
				StringBuffer b = new StringBuffer();
				if (r.peek() == -1) {
					close(); 
					return; 
				} else if (r.peek() == ' ') {
					while (r.peek() == ' ') b.append((char)r.read());
				} else if (r.peek() == '\n') {
					b.append((char)r.read());
				} else if (Character.isDigit(r.peek())) { // consume number
					while (Character.isDigit(r.peek())) b.append((char)r.read());
					if (r.peek() == '.' && Character.isDigit(r.peek(1))) b.append((char)r.read());
					while (Character.isDigit(r.peek())) b.append((char)r.read());
				} else if (Character.isAlphabetic(r.peek())) { // consume alpha digit
					while (Character.isAlphabetic(r.peek()) || Character.isDigit(r.peek())) {
						b.append((char)r.read());
					}
				} else { // consume character at a time
					b.append((char)r.read());
				}
				next = b.toString();
			} catch (IOException e) {
				e.printStackTrace();
				try { r.close(); } catch (IOException x) {}
				r = null;
			}
			
		}
	}

	public boolean hasNext() {
		prepareNext();
		return next != null;
	}

	public String peek() {
		prepareNext(); 
		return next;
	}
	
	public String next() {
		prepareNext(); 
		String rv = next; 
		next = null;
		return rv;
	}

	public void remove() {}

}

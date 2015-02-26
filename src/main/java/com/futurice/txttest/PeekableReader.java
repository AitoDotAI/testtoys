package com.futurice.txttest;

import java.io.IOException;
import java.io.Reader;

public class PeekableReader {
	
	private Reader r;

	// rotating peek buffer
	private int[] peeks = new int[256];
	
	private int at, n;
	
	private int pos;
	
	public PeekableReader(Reader r) {
		this.r = r;
		pos = 0; 
	}
	
	public void havePeek(int i) throws IOException {
		while (i >= n) {
			peeks[(at+n)%peeks.length] = r.read(); n++;
		}
	}

	public void close() throws IOException {
		r.close(); 
	}
	
	public int peek() throws IOException {
		havePeek(0);
		return peeks[at];
	}

	public int peek(int i) throws IOException {
		if (i >= peeks.length) throw new RuntimeException("a peek too far, max="+peeks.length);
		havePeek(i);
		return peeks[(at+i)%peeks.length];
	}

	public int pos() {
		return pos; 
	}

	public int read() throws IOException {
		int rv = -1;
		if (n > 0) {
			rv = peeks[at]; 
			at = (at+1)%peeks.length; n--;
		} else {
			rv = r.read();
		}
		pos++; 
		return rv;
	}

	public String toString() {
		StringBuffer rv = new StringBuffer();
		for (int i = 0; i < n; ++i) {
			try {
				int c = peek(i);
				if (c == -1) break;
				rv.append((char)c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rv.toString();
		
	}

}

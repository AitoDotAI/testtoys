package com.futurice.testtoys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TestTool {
	
	public final static int INTERACTIVE   = 0; 
	public final static int AUTOMATIC_FREEZE = 1;
	public final static int NEVER_FREEZE  = 2;
	
	public final static String UNDEFINED = "__UNDEFINED__";
	public final static String EOF = "__EOF__";
	
	private File filePath;
	private FileWriter out;
	private PrintStream report;
	private BufferedReader exp;
	private TestTokenizer expl; // line tokens 
	private File outFile; 
	private File expFile; 
	private int errors = 0;
	private boolean lineOk = true;
	private int errorPos = -1; 
	private String errorExp;
	private int config;
	private long beginMs;
	private StringBuffer outline; 
	private String expline;
	
	public TestTool(String path) throws IOException {
		this(new File(path), System.out, System.getenv("TESTTOYS_NEVER_FREEZE") != null ? NEVER_FREEZE : INTERACTIVE);
	}
	public TestTool(String path, PrintStream report) throws IOException {
		this(new File(path), report, System.getenv("TESTTOYS_NEVER_FREEZE") != null ? NEVER_FREEZE : INTERACTIVE);
	}
	public TestTool(String path, int config) throws IOException {
		this(new File(path), System.out, config);
	}
	public TestTool(String path, PrintStream report, int config) throws IOException {
		this(new File(path), report, config);
	}
	public TestTool(File path, PrintStream report, int config) throws IOException {
		this.config = config;
		path.getParentFile().mkdirs();
		if (path.exists()) {
			for (File f : path.listFiles()) {
				f.delete();
			}
			path.delete(); 
		}
		
		this.report = report;
		outFile = new File(path + "_out.txt");
		expFile = new File(path + "_exp.txt");
		this.filePath = new File(path + "_out");
		
		out = new FileWriter(outFile);
		try {
			exp = new BufferedReader(new FileReader(expFile)); 
		} catch (FileNotFoundException e) {
			exp = null;
		}
		prepareLine();
		report.println("running " + path + "...\n");
		beginMs = System.currentTimeMillis();
	}
	public void close() throws IOException {
		out.close(); 
		exp.close(); 
	}
	public File fileDir() {
		filePath.mkdir(); 
		return filePath;
	}
	public File file(String filename) {
		filePath.mkdir(); 
		return new File(filePath, filename);
	}
	public File dataFile(String filename) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hhmm");
		return new File(filePath + "_" + filename + "_" + sdf.format(new Date()));
	}
	public void prepareLine() throws IOException {
		outline = new StringBuffer(); 
		if (exp != null) {
			expline = exp.readLine();
			if (expline != null) {
				expl = new TestTokenizer(new StringReader(expline + "\n")); 
			}
		}
		lineOk = true;
		errorPos = -1;
		errorExp = null; 
	}
	public List<String> parse(String s) {
		return TestTokenizer.split(s); 
	}
	public boolean isExp() {
		return expl != null; 
	}
	public String read() throws IOException {
		if (expl == null) return (exp == null?UNDEFINED:EOF);
		return expl.next();
	}
	public String peek() throws IOException  {
		if (expl == null) return (exp == null?UNDEFINED:EOF);
		return expl.peek();
	}
	
	public Double peekDouble() throws IOException {
		try {
			return Double.parseDouble(peek());
		} catch (NumberFormatException e) {
			return null; 
		} catch (NullPointerException e) {
			return null; 
		}
	}
	public Long peekLong() throws IOException {
		try { 
			return Long.parseLong(peek());
		} catch (NumberFormatException e) {
			return null; 
		}
	}
	
	void lineDone() throws IOException {
		if (!lineOk) {
			errors++;
			report.print("! " + outline.toString());
			if (expline != null) {
				int pad = 50 - 2 - outline.length(); 
				for (int i = 0; i < pad; ++i) report.print(' ');
				report.println("|" + expline.toString());
			} else {
				report.println();
			}
		} else {
			report.println("  " + outline.toString());
		}
		prepareLine(); 
	}
	public boolean test(Object t, String e) {
		if (e == UNDEFINED) return true; 
		if (e == null) return false;
		return t.equals(e);
	}
	public void ignoreToken(Object t) throws IOException {
		read(); // skip the token
		out.write(t.toString());
		if (t.equals("\n")) {
			lineDone(); 
		} else {
			outline.append(t);
		}
	}
	public void feedToken(Object t) throws IOException {
		String e = read();
		if (!test(t, e)) {
			lineOk = false;
			if (errorPos == -1) {
				errorPos = outline.length();
				errorExp = e;
			}
		}
		out.write(t.toString());
		if (t.equals("\n")) {
			lineDone(); 
		} else {
			outline.append(t);
		}
	}
	public int pos() {
		return outline.length();
	}
	public void t(String s) throws IOException {
		for (String t : parse(s)) {
			feedToken(t);
		}
	}
	public void i(Object t) throws IOException {
		ignoreToken(t); 
	}
	public void i(String s) throws IOException {
		for (String t : parse(s)) {
			ignoreToken(t);
		}
	}
	public void igf(String f, Object...args) throws IOException {
		i(String.format(f, args));  
	}
	public void iln(String s) throws IOException {
		i(s); 
		ignoreToken("\n"); 
	}
	public void ifln(String f, Object...args) throws IOException {
		igf(f, args);  
		ignoreToken("\n");
	}
	public void tln(String s) throws IOException {
		t(s);
		feedToken("\n"); 
	}
	public void tf(String s, Object... args) throws IOException {
		t(String.format(s, args));
	}
	public void tfln(String s, Object... args) throws IOException {
		tf(s, args);
		feedToken("\n"); 
	}
	public void tln() throws IOException {
		feedToken("\n"); 
	}
	public void t(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		String l;
		while ((l = r.readLine()) != null) tln(l);
		r.close(); 
	}
	
	public boolean done() throws IOException {
		boolean ok = errors == 0;
		long ms = System.currentTimeMillis() - beginMs;
		
		if (outline.length() > 0) {
			lineDone();
		}
		out.close();
		if (exp != null) exp.close();
		
		report.println();
		report.print(ms + " ms. ");
		if (errors > 0) {
			report.print(errors + " errors! ");
		} 

		if (exp == null || errors > 0) {
			boolean freeze = (config == AUTOMATIC_FREEZE);
			if (config == INTERACTIVE) {
				while (true) {
					System.out.print("[d]iff, [c]ontinue or [f]reeze?"); 
					String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
					if (line.equals("d")) {
					    String[] params = new String [3];
					    params[0] = "/usr/bin/meld";
					    params[1] = expFile.getAbsolutePath();
					    params[2] = outFile.getAbsolutePath();
					    Runtime.getRuntime().exec(params);
					} else if (line.equals("f")) {
						freeze = true;
						break;
					} else if (line.equals("c")){
						freeze = false;
						break;
					}
				}
			}
			if (freeze) {
				outFile.renameTo(expFile);
				report.println("frozen.");
				ok = true; 
			} 
		} 
		report.println(); 
		return ok; 
	}
}

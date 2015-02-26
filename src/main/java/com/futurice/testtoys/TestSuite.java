package com.futurice.testtoys;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestSuite {
	private class TestEntry {
		final String PATH;
		final TestCase TEST;
		public TestEntry(String path, TestCase test) {
			PATH = path; TEST = test;
		}
	}
	private File rootPath;
	private List<TestEntry> tests;
	public TestSuite(File rootPath) {
		this.rootPath = rootPath; 
		tests = new ArrayList<TestEntry>();
	}
	public void add(String path, TestCase test) {
		tests.add(new TestEntry(path, test)); 
	}
	public void exec(String[] args) throws IOException {
		exec(args, TestTool.INTERACTIVE);
	}
	public void exec(String[] args, int config) throws IOException {
		// TODO, FIXME
		if (args.length == 0) {
			exec("*");
		} else {
			for (String arg : args) {
				exec(arg, config);
			}
		}
	}
	public void exec(String selection) throws IOException {
		exec(selection, TestTool.INTERACTIVE);
	}
	public boolean matches(String[] selection, String[] path) {
		for (int i = 0; i < selection.length; ++i) {
			if (selection[i].endsWith("*")) {
				if (!path[i].startsWith(selection[i].substring(0, selection[i].length()-1))) return false;
			} else if (!selection[i].equals(path[i])) {
				return false;
			}
		}
		return true; 
	}
	public void exec(String selection, int config) throws IOException {
		String s[] = selection.split("/");
		for (TestEntry e : tests) {
			String p[] = e.PATH.split("/");
			if (matches(s, p)) {
				TestTool t = new TestTool(new File(rootPath, e.PATH), System.out, config); 
				try {
					e.TEST.test(t);
				} catch (Exception x) {
					StringWriter w = new StringWriter();
					x.printStackTrace(new PrintWriter(w));
					t.t(w.toString()); 
					w.close(); 
				}
				t.done(); 
			}
		}
	}
}

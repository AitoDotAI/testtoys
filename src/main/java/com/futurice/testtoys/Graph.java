package com.futurice.txttest;

public class Graph {
	
	private double[] values;
	
	public Graph(double[] values) {
		this.values = values; 
	}

	public double max() {
		double max = 0;
		for (Double d : values) {
			max = Math.max(max, d);
		}
		return max;
	}

	public double min() {
		double min = Double.MAX_VALUE;
		for (Double d : values) {
			min = Math.min(min, d);
		}
		return min;
	}

	public String toString(int height) {
		StringBuffer rv = new StringBuffer(); 
		double max = max(); 
		double min = min(); 
		for (int j = 0; j < values.length; ++j) rv.append("-"); 
		rv.append('\n');
		for (int i = 0; i < height; ++i)  {
			double low = min + (height-1-i) * ((max-min) / height);
			double high = min + (height-i) * ((max-min) / height);
			for (int j = 0; j < values.length; ++j) {
				if (values[j] >= low && values[j] <= high) {
					rv.append('o');
				} else if (values[j] >= high) {
					rv.append('|');
				} else {
					rv.append(' ');
				}
			}
			rv.append("   " + String.format("%.03f", 0.5*(low+high)) + "\n");
		}
		for (int j = 0; j < values.length; ++j) rv.append("-"); 
		rv.append('\n');

		return rv.toString();
	}
}

package com.futurice.txttest;

import scala.math.Numeric;

public class RelativeRange {
	
	private final Number VALUE; 
	
	private final double RANGE;
	
	public RelativeRange(Number v, double range) {
		VALUE = v; 
		RANGE = range;
	}
	
	public boolean equals(Object o) {
		boolean rv = false;
		if (o instanceof String) {
			try {
				double d = Double.parseDouble(((String)o));
				rv = d >= VALUE.doubleValue()*(1/RANGE)
				  && d <= VALUE.doubleValue()*RANGE;
			} catch (NumberFormatException e) {}
		}
		return rv;
	}

	public String toString() {
		return VALUE.toString(); 
	}
}

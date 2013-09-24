package yabby.core.parameter;


import java.io.PrintStream;

import yabby.core.Description;



@Description("Parameter represented by a string value")
public class StringParameter extends Parameter.BaseP<String> {

	@Override
	public double getArrayValue() {return 0;}

	@Override
	public double getArrayValue(int iDim) {return 0;}

	@Override
	public void log(int nSample, PrintStream out) {
		// TODO Auto-generated method stub
	}


	@Override
	void fromXML(int nDimension, String sLower, String sUpper, String[] sValues) {
		// TODO Auto-generated method stub

	}

	@Override
	public int scale(double fScale) throws Exception {
		// ignore -- cannot scale String
		return 0;
	}

	@Override
	String getMax() {
		return null;
	}

	@Override
	String getMin() {
		return null;
	}

}

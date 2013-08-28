/*
* File Parameter.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is part of BEAST2.
* See the NOTICE file distributed with this work for additional
* information regarding copyright ownership and licensing.
*
* BEAST is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
*  BEAST is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with BEAST; if not, write to the
* Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
* Boston, MA  02110-1301  USA
*/
package yabby.core.parameter;



import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import yabby.core.*;
import yabby.core.Input.Validate;


@Description("A parameter represents a value in the state space that can be changed " +
        "by operators.")
public abstract class Parameter<T> extends StateNode implements Function {
    /**
     * value is a required input since it is very hard to ensure any internal consistency
     * when no value is specified. When	another class wants to set the dimension, say,
     * this will make it the responsibility of the other class to maintain internal consistency of
     * the parameter.
     */
    public Input<String> valuesInput = new Input<String>("value", "start value(s) for this parameter. If multiple values are specified, they should be separated by whitespace.", Validate.REQUIRED);
    public final Input<java.lang.Integer> m_nDimension =
            new Input<java.lang.Integer>("dimension", "dimension of the parameter (default 1, i.e scalar)", 1);
    public final Input<Integer> minorDimensionInput = new Input<Integer>("minordimension", "minor-dimension when the parameter is interpreted as a matrix (default 1)", 1);


    /**
     * constructors *
     */
    public Parameter() {
    }

    public Parameter(final T[] values) {
        this.values = values.clone();
        this.storedValues = values.clone();
        m_fUpper = getMax();
        m_fLower = getMin();
        m_bIsDirty = new boolean[values.length];
    }

    @Override
    public void initAndValidate() throws Exception {
        m_bIsDirty = new boolean[m_nDimension.get()];

        minorDimension = minorDimensionInput.get();
        if (minorDimension > 0 && m_nDimension.get() % minorDimension > 0) {
            throw new Exception("Dimension must be divisble by stride");
        }
        this.storedValues = values.clone();
    }


    /**
     * upper & lower bound *
     */
    protected T m_fUpper;
    protected T m_fLower;

    abstract T getMax();

    abstract T getMin();

    /**
     * the actual values of this parameter
     */
    protected T[] values;
    protected T[] storedValues;

    /**
     * sub-dimension when parameter is considered a matrix
     */
    protected int minorDimension = 1;

    /**
     * isDirty flags for individual elements in high dimensional parameters
     */
    protected boolean[] m_bIsDirty;
    /**
     * last element to be changed *
     */
    protected int m_nLastDirty;

    /**
     * @param iParam dimention to check
     * @return true if the iParam-th element has changed
     */
    public boolean isDirty(final int iParam) {
        return m_bIsDirty[iParam];
    }

    /**
     * Returns index of entry that was changed last. Useful if it is known only a
     * single  value has changed in the array. *
     */
    public int getLastDirty() {
        return m_nLastDirty;
    }

    @Override
    public void setEverythingDirty(final boolean isDirty) {
        setSomethingIsDirty(isDirty);
        Arrays.fill(m_bIsDirty, isDirty);
    }

    /*
     * various setters & getters *
     */
    public int getDimension() {
        return values.length;
    }

    /**
     * Change the dimension of a parameter
     * <p/>
     * This should only be called from initAndValidate() when a parent
     * plugin can easily calculate the dimension of a parameter, but it
     * is awkward to do this by hand.
     * <p/>
     * Values are sourced from the original parameter values.
     * @param nDimension
     */
    @SuppressWarnings("unchecked")
    public void setDimension(final int nDimension) {
        if (getDimension() != nDimension) {
            final T[] values2 = (T[]) Array.newInstance(m_fUpper.getClass(), nDimension);
            for (int i = 0; i < nDimension; i++) {
                values2[i] = values[i % getDimension()];
            }
            values = (T[]) values2;
            //storedValues = (T[]) Array.newInstance(m_fUpper.getClass(), nDimension);
        }
        m_bIsDirty = new boolean[nDimension];
    }
    
    public void setMinorDimension(final int nDimension) throws Exception {
        minorDimension = nDimension;
        if (minorDimension > 0 && m_nDimension.get() % minorDimension > 0) {
            throw new Exception("Dimension must be divisble by stride");
        }
    }
    
    public T getValue() {
        return values[0];
    }

    public T getLower() {
        return m_fLower;
    }

    public void setLower(final T fLower) {
        m_fLower = fLower;
    }

    public T getUpper() {
        return m_fUpper;
    }

    public void setUpper(final T fUpper) {
        m_fUpper = fUpper;
    }

    public T getValue(final int iParam) {
        return values[iParam];
    }

    public T[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    public void setBounds(final T fLower, final T fUpper) {
        m_fLower = fLower;
        m_fUpper = fUpper;
    }

    public void setValue(final T fValue) {
        startEditing(null);
//        if (isStochastic()) {
        values[0] = fValue;
        m_bIsDirty[0] = true;
        m_nLastDirty = 0;
//    	}
//        } else {
//        	System.err.println("Can't set the value of a parameter, unless startEditing() is called first.");
//        	System.exit(1);
//        }
    }

    public void setValue(final int iParam, final T fValue) {
        startEditing(null);
//        if (isStochastic()) {
        values[iParam] = fValue;
        m_bIsDirty[iParam] = true;
        m_nLastDirty = iParam;
//        } else {
//        	System.err.println("Can't set the value of a parameter, unless startEditing() is called first.");
//	        System.exit(1);
//	    }
    }

    public void swap(final int iLeft, final int iRight) {
        startEditing(null);
        final T tmp = values[iLeft];
        values[iLeft] = values[iRight];
        values[iRight] = tmp;
    }

    /*public void setValueQuietly(int dim, T value){
        values[dim] = value;
        m_bIsDirty[dim] = true;
        m_nLastDirty = dim;
    }*/

    /**
     * Note that changing toString means fromXML needs to be changed as well,
     * since it parses the output of toString back into a parameter.
     */
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(ID).append("[").append(values.length);
        if (minorDimension > 0) {
            buf.append(" ").append(minorDimension);
        }
        buf.append("] ");
        buf.append("(").append(m_fLower).append(",").append(m_fUpper).append("): ");
        for (final T value : values) {
            buf.append(value).append(" ");
        }
        return buf.toString();
    }


    @Override
    public Parameter<T> copy() {
        try {
            @SuppressWarnings("unchecked") final
            Parameter<T> copy = (Parameter<T>) this.clone();
            copy.values = values.clone();//new Boolean[values.length];
            copy.m_bIsDirty = new boolean[values.length];
            return copy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void assignTo(final StateNode other) {
        @SuppressWarnings("unchecked") final
        Parameter<T> copy = (Parameter<T>) other;
        copy.setID(getID());
        copy.index = index;
        copy.values = values.clone();
        //System.arraycopy(values, 0, copy.values, 0, values.length);
        copy.m_fLower = m_fLower;
        copy.m_fUpper = m_fUpper;
        copy.m_bIsDirty = new boolean[values.length];
    }

    @Override
    public void assignFrom(final StateNode other) {
        @SuppressWarnings("unchecked") final
        Parameter<T> source = (Parameter<T>) other;
        setID(source.getID());
        values = source.values.clone();
        storedValues = source.storedValues.clone();
        System.arraycopy(source.values, 0, values, 0, values.length);
        m_fLower = source.m_fLower;
        m_fUpper = source.m_fUpper;
        m_bIsDirty = new boolean[source.values.length];
    }

    @Override
    public void assignFromFragile(final StateNode other) {
        @SuppressWarnings("unchecked") final
        Parameter<T> source = (Parameter<T>) other;
        System.arraycopy(source.values, 0, values, 0, values.length);
        Arrays.fill(m_bIsDirty, false);
    }


    /**
     * Loggable interface implementation follows (partly, the actual
     * logging of values happens in derived classes) *
     */
    @Override
    public void init(final PrintStream out) throws Exception {
        final int nValues = getDimension();
        if (nValues == 1) {
            out.print(getID() + "\t");
        } else {
            for (int iValue = 0; iValue < nValues; iValue++) {
                out.print(getID() + (iValue + 1) + "\t");
            }
        }
    }

    @Override
    public void close(final PrintStream out) {
        // nothing to do
    }

    /**
     * StateNode implementation *
     */
    @Override
    public void fromXML(final Node node) {
        final NamedNodeMap atts = node.getAttributes();
        setID(atts.getNamedItem("id").getNodeValue());
        final String sStr = node.getTextContent();
        Pattern pattern = Pattern.compile(".*\\[(.*) (.*)\\].*\\((.*),(.*)\\): (.*) ");
        Matcher matcher = pattern.matcher(sStr);
        if (matcher.matches()) {
            final String sDimension = matcher.group(1);
            final String sStride = matcher.group(2);
            final String sLower = matcher.group(3);
            final String sUpper = matcher.group(4);
            final String sValuesAsString = matcher.group(5);
            final String[] sValues = sValuesAsString.split(" ");
            minorDimension = Integer.parseInt(sStride);
            fromXML(Integer.parseInt(sDimension), sLower, sUpper, sValues);
        } else {
            pattern = Pattern.compile(".*\\[(.*)\\].*\\((.*),(.*)\\): (.*) ");
            matcher = pattern.matcher(sStr);
            if (matcher.matches()) {
                final String sDimension = matcher.group(1);
                final String sLower = matcher.group(2);
                final String sUpper = matcher.group(3);
                final String sValuesAsString = matcher.group(4);
                final String[] sValues = sValuesAsString.split(" ");
                minorDimension = 0;
                fromXML(Integer.parseInt(sDimension), sLower, sUpper, sValues);
            } else {
                throw new RuntimeException("parameter could not be parsed");
            }
        }
    }

    /**
     * Restore a saved parameter from string representation.
     * This cannot be a template method since it requires
     * creation of an array of T...
     *
     * @param nDimension parameter dimention
     * @param sLower     lower bound
     * @param sUpper     upper bound
     * @param sValues    values
     */
    abstract void fromXML(int nDimension, String sLower, String sUpper, String[] sValues);

    /**
     * matrix implementation *
     */
    public int getMinorDimension1() {
        return minorDimension;
    }

    public int getMinorDimension2() {
        return getDimension() / minorDimension;
    }

    public T getMatrixValue(final int i, final int j) {
        return values[i * minorDimension + j];
    }

    public void setMatrixValue(final int i, final int j, final T value) {
        setValue(i * minorDimension + j, value);
    }

    public void getMatrixValues1(final int i, final T[] row) {
        assert (row.length == minorDimension);
        System.arraycopy(values, i * minorDimension, row, 0, minorDimension);
    }

    public void getMatrixValues1(final int i, final double[] row) {
        assert (row.length == minorDimension);
        for (int j = 0; j < minorDimension; j++) {
            row[j] = getArrayValue(i * minorDimension + j);
        }
    }

    public void getMatrixValues2(final int j, final T[] col) {
        assert (col.length == getMinorDimension2());
        for (int i = 0; i < getMinorDimension2(); i++) {
            col[i] = values[i * minorDimension + j];
        }
    }

    public void getMatrixValues2(final int j, final double[] col) {
        assert (col.length == getMinorDimension2());
        for (int i = 0; i < getMinorDimension2(); i++) {
            col[i] = getArrayValue(i * minorDimension + j);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void store() {
    	if (storedValues.length != values.length) {
    		storedValues = (T[]) Array.newInstance(m_fUpper.getClass(), values.length);
    	}
        System.arraycopy(values, 0, storedValues, 0, values.length);
    }

    @Override
    public void restore() {
        final T[] tmp = storedValues;
        storedValues = values;
        values = tmp;
        hasStartedEditing = false;
    	if (m_bIsDirty.length != values.length) {
    		m_bIsDirty = new boolean[values.length];
    	}
    }
} // class Parameter

/*
* File GeneralSubstitutionModel.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is not copyright Remco! It is copied from BEAST 1.
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
package yabby.evolution.substitutionmodel;


import java.lang.reflect.Constructor;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Function;
import yabby.core.Input.Validate;
import yabby.evolution.datatype.DataType;
import yabby.evolution.tree.Node;


@Description("Specifies transition probability matrix with no restrictions on the rates other " +
        "than that one of the is equal to one and the others are specified relative to " +
        "this unit rate. Works for any number of states.")
public class GeneralSubstitutionModel extends SubstitutionModel.Base {
    public Input<Function> m_rates =
            new Input<Function>("rates", "Rate parameter which defines the transition rate matrix. " +
                    "Only the off-diagonal entries need to be specified (diagonal makes row sum to zero in a " +
                    "rate matrix). Entry i specifies the rate from floor(i/(n-1)) to i%(n-1)+delta where " +
                    "n is the number of states and delta=1 if floor(i/(n-1)) >= i%(n-1) and 0 otherwise.", Validate.REQUIRED);

    public Input<String> eigenSystemClass = new Input<String>("eigenSystem", "Name of the class used for creating an EigenSystem", DefaultEigenSystem.class.getName());
    /**
     * a square m_nStates x m_nStates matrix containing current rates  *
     */
    double[][] m_rateMatrix;


    @Override
    public void initAndValidate() throws Exception {
        super.initAndValidate();
        updateMatrix = true;
        m_nStates = m_frequencies.getFreqs().length;
        if (m_rates.get().getDimension() != m_nStates * (m_nStates - 1)) {
            throw new Exception("Dimension of input 'rates' is " + m_rates.get().getDimension() + " but a " +
                    "rate matrix of dimension " + m_nStates + "x" + (m_nStates - 1) + "=" + m_nStates * (m_nStates - 1) + " was " +
                    "expected");
        }

        eigenSystem = createEigenSystem();
        //eigenSystem = new DefaultEigenSystem(m_nStates);

        m_rateMatrix = new double[m_nStates][m_nStates];
        relativeRates = new double[m_rates.get().getDimension()];
        storedRelativeRates = new double[m_rates.get().getDimension()];
    } // initAndValidate

    /**
     * create an EigenSystem of the class indicated by the eigenSystemClass input *
     */
    protected EigenSystem createEigenSystem() throws Exception {
        Constructor<?>[] ctors = Class.forName(eigenSystemClass.get()).getDeclaredConstructors();
        Constructor<?> ctor = null;
        for (int i = 0; i < ctors.length; i++) {
            ctor = ctors[i];
            if (ctor.getGenericParameterTypes().length == 1)
                break;
        }
        ctor.setAccessible(true);
        return (EigenSystem) ctor.newInstance(m_nStates);
    }

    protected double[] relativeRates;
    protected double[] storedRelativeRates;

    protected EigenSystem eigenSystem;

    protected EigenDecomposition eigenDecomposition;
    private EigenDecomposition storedEigenDecomposition;

    protected boolean updateMatrix = true;
    private boolean storedUpdateMatrix = true;

    @Override
    public void getTransitionProbabilities(Node node, double fStartTime, double fEndTime, double fRate, double[] matrix) {
        double distance = (fStartTime - fEndTime) * fRate;

        int i, j, k;
        double temp;

        // this must be synchronized to avoid being called simultaneously by
        // two different likelihood threads - AJD
        synchronized (this) {
            if (updateMatrix) {
                setupRelativeRates();
                setupRateMatrix();
                eigenDecomposition = eigenSystem.decomposeMatrix(m_rateMatrix);
                updateMatrix = false;
            }
        }

        // is the following really necessary?
        // implemented a pool of iexp matrices to support multiple threads
        // without creating a new matrix each call. - AJD
        // a quick timing experiment shows no difference - RRB
        double[] iexp = new double[m_nStates * m_nStates];
        // Eigen vectors
        double[] Evec = eigenDecomposition.getEigenVectors();
        // inverse Eigen vectors
        double[] Ievc = eigenDecomposition.getInverseEigenVectors();
        // Eigen values
        double[] Eval = eigenDecomposition.getEigenValues();
        for (i = 0; i < m_nStates; i++) {
            temp = Math.exp(distance * Eval[i]);
            for (j = 0; j < m_nStates; j++) {
                iexp[i * m_nStates + j] = Ievc[i * m_nStates + j] * temp;
            }
        }

        int u = 0;
        for (i = 0; i < m_nStates; i++) {
            for (j = 0; j < m_nStates; j++) {
                temp = 0.0;
                for (k = 0; k < m_nStates; k++) {
                    temp += Evec[i * m_nStates + k] * iexp[k * m_nStates + j];
                }

                matrix[u] = Math.abs(temp);
                u++;
            }
        }
    } // getTransitionProbabilities

    /**
     * access to (copy of) rate matrix *
     */
    protected double[][] getRateMatrix() {
        return m_rateMatrix.clone();
    }

    protected void setupRelativeRates() {
        Function rates = m_rates.get();
        for (int i = 0; i < rates.getDimension(); i++) {
            relativeRates[i] = rates.getArrayValue(i);
        }
    }

    /**
     * sets up rate matrix *
     */
    protected void setupRateMatrix() {
        double[] fFreqs = m_frequencies.getFreqs();
        for (int i = 0; i < m_nStates; i++) {
            m_rateMatrix[i][i] = 0;
            for (int j = 0; j < i; j++) {
                m_rateMatrix[i][j] = relativeRates[i * (m_nStates - 1) + j];
            }
            for (int j = i + 1; j < m_nStates; j++) {
                m_rateMatrix[i][j] = relativeRates[i * (m_nStates - 1) + j - 1];
            }
        }
        // bring in frequencies
        for (int i = 0; i < m_nStates; i++) {
            for (int j = i + 1; j < m_nStates; j++) {
                m_rateMatrix[i][j] *= fFreqs[j];
                m_rateMatrix[j][i] *= fFreqs[i];
            }
        }
        // set up diagonal
        for (int i = 0; i < m_nStates; i++) {
            double fSum = 0.0;
            for (int j = 0; j < m_nStates; j++) {
                if (i != j)
                    fSum += m_rateMatrix[i][j];
            }
            m_rateMatrix[i][i] = -fSum;
        }
        // normalise rate matrix to one expected substitution per unit time
        double fSubst = 0.0;
        for (int i = 0; i < m_nStates; i++)
            fSubst += -m_rateMatrix[i][i] * fFreqs[i];

        for (int i = 0; i < m_nStates; i++) {
            for (int j = 0; j < m_nStates; j++) {
                m_rateMatrix[i][j] = m_rateMatrix[i][j] / fSubst;
            }
        }
    } // setupRateMatrix


    /**
     * CalculationNode implementation follows *
     */
    @Override
    public void store() {
        storedUpdateMatrix = updateMatrix;
        storedEigenDecomposition = eigenDecomposition.copy();
//        System.arraycopy(relativeRates, 0, storedRelativeRates, 0, relativeRates.length);

        super.store();
    }

    /**
     * Restore the additional stored state
     */
    @Override
    public void restore() {

        updateMatrix = storedUpdateMatrix;

        // To restore all this stuff just swap the pointers...
//        double[] tmp1 = storedRelativeRates;
//        storedRelativeRates = relativeRates;
//        relativeRates = tmp1;

        EigenDecomposition tmp = storedEigenDecomposition;
        storedEigenDecomposition = eigenDecomposition;
        eigenDecomposition = tmp;
        super.restore();

    }

    @Override
    protected boolean requiresRecalculation() {
        // we only get here if something is dirty
        updateMatrix = true;
        return true;
    }


    /**
     * This function returns the Eigen vectors.
     *
     * @return the array
     */
    @Override
    public EigenDecomposition getEigenDecomposition(Node node) {
        synchronized (this) {
            if (updateMatrix) {
                setupRelativeRates();
                setupRateMatrix();
                eigenDecomposition = eigenSystem.decomposeMatrix(m_rateMatrix);
                updateMatrix = false;
            }
        }
        return eigenDecomposition;
    }

    @Override
    public boolean canHandleDataType(DataType dataType) throws Exception {
        if (dataType.getStateCount() == Integer.MAX_VALUE) {
            return false;
        }
        return true;
    }

} // class GeneralSubstitutionModel
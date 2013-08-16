/*
* File Alignment.java
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
package yabby.evolution.alignment;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.evolution.datatype.DataType;
import yabby.util.AddOnManager;

/* Class representing alignment data.
 * **/

@Description("Class representing alignment data")
public class Alignment extends CalculationNode {
    /**
     * default data type *
     */
    final static String NUCLEOTIDE = "nucleotide";

    /**
     * directory to pick up data types from *
     */
    final static String[] IMPLEMENTATION_DIR = {"yabby.evolution.datatype"};

    /**
     * list of data type descriptions, obtained from DataType classes *
     */
    static List<String> m_sTypes = new ArrayList<String>();

    static {
    	findDataTypes();
    }
    
    static public void findDataTypes() {
        // build up list of data types
        List<String> m_sDataTypes = AddOnManager.find(yabby.evolution.datatype.DataType.class, IMPLEMENTATION_DIR);
        for (String sDataType : m_sDataTypes) {
            try {
                DataType dataType = (DataType) Class.forName(sDataType).newInstance();
                if (dataType.isStandard()) {
                    String sDescription = dataType.getDescription();
                    if (!m_sTypes.contains(sDescription)) {
                    	m_sTypes.add(sDescription);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }


    public Input<List<Sequence>> sequenceInput =
            new Input<List<Sequence>>("sequence", "sequence and meta data for particular taxon", new ArrayList<Sequence>(), Validate.REQUIRED);
    public Input<Integer> stateCountInput = new Input<Integer>("statecount", "maximum number of states in all sequences");
    //public Input<String> m_sDataType = new Input<String>("dataType", "data type, one of " + Arrays.toString(TYPES), NUCLEOTIDE, TYPES);
    public Input<String> dataTypeInput = new Input<String>("dataType", "data type, one of " + m_sTypes, NUCLEOTIDE, m_sTypes.toArray(new String[0]));
    public Input<DataType.Base> userDataTypeInput= new Input<DataType.Base>("userDataType", "non-standard, user specified data type, if specified 'dataType' is ignored");
    public Input<Boolean> stripInvariantSitesInput = new Input<Boolean>("strip", "sets weight to zero for sites that are invariant (e.g. all 1, all A or all unkown)", false);

    /**
     * list of taxa names defined through the sequences in the alignment *
     */
    protected List<String> taxaNames = new ArrayList<String>();

    /**
     * list of state counts for each of the sequences, typically these are
     * constant throughout the whole alignment.
     */
    protected List<Integer> stateCounts = new ArrayList<Integer>();

    /**
     * maximum of m_nStateCounts *
     */
    protected int maxStateCount;

    /**
     * state codes for the sequences *
     */
    protected List<List<Integer>> counts = new ArrayList<List<Integer>>();

    /**
     * data type, useful for converting String sequence to Code sequence, and back *
     */
    protected DataType m_dataType;

    /**
     * weight over the columns of a matrix *
     */
    protected int[] patternWeight;

    /**
     * pattern state encodings *
     */
    protected int[][] sitePatterns; // #patters x #taxa

    /**
     * maps site nr to pattern nr *
     */
    protected int[] patternIndex;


    public Alignment() {
    }

    /**
     * Constructor for testing purposes.
     *
     * @param sequences
     * @param stateCount
     * @param dataType
     * @throws Exception when validation fails
     */
    public Alignment(List<Sequence> sequences, Integer stateCount, String dataType) throws Exception {

        for (Sequence sequence : sequences) {
            sequenceInput.setValue(sequence, this);
        }
        //m_nStateCount.setValue(stateCount, this);
        dataTypeInput.setValue(dataType, this);
        initAndValidate();
    }


    @Override
    public void initAndValidate() throws Exception {
        // determine data type, either user defined or one of the standard ones
        if (userDataTypeInput.get() != null) {
            m_dataType = userDataTypeInput.get();
        } else {
            if (m_sTypes.indexOf(dataTypeInput.get()) < 0) {
                throw new Exception("data type + '" + dataTypeInput.get() + "' cannot be found. " +
                        "Choose one of " + Arrays.toString(m_sTypes.toArray(new String[0])));
            }
            List<String> sDataTypes = AddOnManager.find(yabby.evolution.datatype.DataType.class, IMPLEMENTATION_DIR);
            for (String sDataType : sDataTypes) {
                DataType dataType = (DataType) Class.forName(sDataType).newInstance();
                if (dataTypeInput.get().equals(dataType.getDescription())) {
                    m_dataType = dataType;
                    break;
                }
            }
        }

        // grab data from child sequences
        taxaNames.clear();
        stateCounts.clear();
        counts.clear();
        for (Sequence seq : sequenceInput.get()) {
            //m_counts.add(seq.getSequence(getMap()));
            counts.add(seq.getSequence(m_dataType));
            if (taxaNames.indexOf(seq.taxonInput.get()) >= 0) {
                throw new Exception("Duplicate taxon found in alignment: " + seq.taxonInput.get());
            }
            taxaNames.add(seq.taxonInput.get());
            stateCounts.add(seq.totalCountInput.get());
        }
        if (counts.size() == 0) {
            // no sequence data
            throw new Exception("Sequence data expected, but none found");
        }

        // Sanity check: make sure sequences are of same length
        int nLength = counts.get(0).size();
        for (List<Integer> seq : counts) {
            if (seq.size() != nLength) {
                throw new Exception("Two sequences with different length found: " + nLength + " != " + seq.size());
            }
        }

        calcPatterns();
    } // initAndValidate


    /*
     * assorted getters and setters *
     */
    public List<String> getTaxaNames() {
        if (taxaNames.size() == 0) {
        	try {
        		initAndValidate();
        	} catch (Exception e) {
        		e.printStackTrace();
        		throw new RuntimeException(e);
        	}
        }
        return taxaNames;
    }

    public List<Integer> getStateCounts() {
        return stateCounts;
    }

    public List<List<Integer>> getCounts() {
        return counts;
    }

    public DataType getDataType() {
        return m_dataType;
    }

    public int getNrTaxa() {
        return taxaNames.size();
    }

    public int getTaxonIndex(String sID) {
        return taxaNames.indexOf(sID);
    }

    public int getPatternCount() {
        return sitePatterns.length;
    }

    public int[] getPattern(int id) {
        return sitePatterns[id];
    }

    public int getPattern(int iTaxon, int id) {
        return sitePatterns[id][iTaxon];
    }

    public int getPatternWeight(int id) {
        return patternWeight[id];
    }

    public int getMaxStateCount() {
        return maxStateCount;
    }

    public int getPatternIndex(int iSite) {
        return patternIndex[iSite];
    }

    public int getSiteCount() {
        return patternIndex.length;
    }

    public int[] getWeights() {
        return patternWeight;
    }


    /**
     * SiteComparator is used for ordering the sites,
     * which makes it easy to identify patterns.
     */
    class SiteComparator implements Comparator<int[]> {
        public int compare(int[] o1, int[] o2) {
            for (int i = 0; i < o1.length; i++) {
                if (o1[i] > o2[i]) {
                    return 1;
                }
                if (o1[i] < o2[i]) {
                    return -1;
                }
            }
            return 0;
        }
    } // class SiteComparator

    /**
     * calculate patterns from sequence data
     * *
     */
    protected void calcPatterns() {
        int nTaxa = counts.size();
        int nSites = counts.get(0).size();

        // convert data to transposed int array
        int[][] nData = new int[nSites][nTaxa];
        for (int i = 0; i < nTaxa; i++) {
            List<Integer> sites = counts.get(i);
            for (int j = 0; j < nSites; j++) {
                nData[j][i] = sites.get(j);
            }
        }

        // sort data
        SiteComparator comparator = new SiteComparator();
        Arrays.sort(nData, comparator);

        // count patterns in sorted data
        int nPatterns = 1;
        int[] weights = new int[nSites];
        weights[0] = 1;
        for (int i = 1; i < nSites; i++) {
            if (comparator.compare(nData[i - 1], nData[i]) != 0) {
                nPatterns++;
                nData[nPatterns - 1] = nData[i];
            }
            weights[nPatterns - 1]++;
        }

        // reserve memory for patterns
        patternWeight = new int[nPatterns];
        sitePatterns = new int[nPatterns][nTaxa];
        for (int i = 0; i < nPatterns; i++) {
            patternWeight[i] = weights[i];
            sitePatterns[i] = nData[i];
        }

        // find patterns for the sites
        patternIndex = new int[nSites];
        for (int i = 0; i < nSites; i++) {
            int[] sites = new int[nTaxa];
            for (int j = 0; j < nTaxa; j++) {
                sites[j] = counts.get(j).get(i);
            }
            patternIndex[i] = Arrays.binarySearch(sitePatterns, sites, comparator);
        }

        // determine maximum state count
        // Usually, the state count is equal for all sites,
        // though for SnAP analysis, this is typically not the case.
        maxStateCount = 0;
        for (int m_nStateCount1 : stateCounts) {
            maxStateCount = Math.max(maxStateCount, m_nStateCount1);
        }
        // report some statistics
        if (taxaNames.size() < 30) {
	        for (int i = 0; i < taxaNames.size(); i++) {
	            System.err.println(taxaNames.get(i) + ": " + counts.get(i).size() + " " + stateCounts.get(i));
	        }
        }
        System.out.println(getNrTaxa() + " taxa");
        System.out.println(getSiteCount() + " sites");
        System.out.println(getPatternCount() + " patterns");


        if (stripInvariantSitesInput.get()) {
            // don't add patterns that are invariant, e.g. all gaps
            System.err.print("Stripping invariant sites");
            for (int i = 0; i < nPatterns; i++) {
                int[] nPattern = sitePatterns[i];
                int iValue = nPattern[0];
                boolean bIsInvariant = true;
                for (int k = 1; k < nPattern.length; k++) {
                    if (nPattern[k] != iValue) {
                        bIsInvariant = false;
                        break;
                    }
                }
                if (bIsInvariant) {
                    patternWeight[i] = 0;
                    System.err.print(" <" + iValue + "> ");
                }
            }
            System.err.println();
        }


    } // calcPatterns


    /**
     * returns an array containing the non-ambiguous states that this state represents.
     */
    public boolean[] getStateSet(int iState) {
        return m_dataType.getStateSet(iState);
//        if (!isAmbiguousState(iState)) {
//            boolean[] stateSet = new boolean[m_nMaxStateCount];
//            stateSet[iState] = true;
//            return stateSet;
//        } else {
//        }
    }

    boolean isAmbiguousState(int state) {
        return (state >= 0 && state < maxStateCount);
    }

} // class Data

package yabby.evolution.datatype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yabby.core.Description;
import yabby.core.YABBYObject;


public interface DataType {
    final static public char GAP_CHAR = '-';
    final static public char MISSING_CHAR = '?';

    /**
     * @return number of states for this data type.
     *         Assuming there is a finite number of states, or -1 otherwise.
     */
    int getStateCount();

    /**
     * Convert a sequence represented by a string into a sequence of integers
     * representing the state for this data type.
     * Ambiguous states should be represented by integer numbers higher than getStateCount()
     * throws exception when parsing error occur *
     */
    List<Integer> string2state(String sSequence) throws Exception;

    /**
     * Convert an array of states into a sequence represented by a string.
     * This is the inverse of string2state()
     * throws exception when State cannot be mapped *
     */
    String state2string(List<Integer> nStates) throws Exception;

    String state2string(int[] nStates) throws Exception;

    /**
     * returns an array of length getStateCount() containing the (possibly ambiguous) states
     * that this state represents.
     */
    public boolean[] getStateSet(int iState);

    /**
     * returns an array with all non-ambiguous states represented by
     * a state.
     */
    public int[] getStatesForCode(int iState);

    boolean isAmbiguousState(int state);

    /**
     * true if the class is completely self contained and does not need any
     * further initialisation. Notable exception: GeneralDataype
     */
    boolean isStandard();

    /**
     * data type description, e.g. nucleotide, codon *
     */
    public String getDescription();

    /**
     * Get character corresponding to a given state
     *
     * @param state state
     *              <p/>
     *              return corresponding character
     */
    public char getChar(int state);

    /**
     * Get a string code corresponding to a given state. By default this
     * calls getChar but overriding classes may return multicharacter codes.
     *
     * @param state state
     *              <p/>
     *              return corresponding code
     */
    public String getCode(int state);

    @Description(value = "Base class bringing class and interfaces together", isInheritable = false)
    public abstract class Base extends YABBYObject implements DataType {
        /**
         * size of the state space *
         */
        int stateCount;

        /**
         * maps string encoding to state codes *
         */
        String codeMap;

        public String getCodeMap() {
            return codeMap;
        }

        /**
         * length of the encoding, e.g. 1 for nucleotide, 3 for codons *
         */
        int codeLength;

        /**
         * mapping codes to sets of states *
         */
        int[][] mapCodeToStateSet;

        @Override
        public void initAndValidate() throws Exception {
            if (mapCodeToStateSet != null) {
                if (mapCodeToStateSet.length != codeMap.length() / codeLength) {
                    throw new Exception("m_sCodeMap and m_mapCodeToStateSet have incompatible lengths");
                }
            }
        }

        @Override
        public int getStateCount() {
            return stateCount;
        }

        /**
         * implementation for single character per state encoding *
         */
        @Override
        public List<Integer> string2state(String data) throws Exception {
            List<Integer> sequence;
            sequence = new ArrayList<Integer>();
            // remove spaces
            data = data.replaceAll("\\s", "");
            data = data.toUpperCase();
            if (codeMap == null) {
                if (data.contains(",")) {
                    // assume it is a comma separated string of integers
                    String[] sStrs = data.split(",");
                    for (String sStr : sStrs) {
                    	try {
                    		sequence.add(Integer.parseInt(sStr));
                    	} catch (NumberFormatException e) {
                    		sequence.add(-1);
                    	}
                    }
                } else {
                    // assume it is a string where each character is a state
                    for (byte c : data.getBytes()) {
                    	switch (c) {
                    	case GAP_CHAR:
                    	case MISSING_CHAR:
                            sequence.add(-1);
                            break;
                    	default:
                    		sequence.add(Integer.parseInt((char) c + ""));
                    	}
                    }
                }
            } else {
                if (codeLength == 1) {
                    // single character codes
                    for (int i = 0; i < data.length(); i++) {
                        char cCode = data.charAt(i);
                        int nState = codeMap.indexOf(cCode);
                        if (nState < 0) {
                            throw new Exception("Unknown code found in sequence: " + cCode);
                        }
                        sequence.add(nState);
                    }
                } else if (codeLength > 1) {
                    // multi-character codes of fixed length

                    // use code map to resolve state codes
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    // fixed length code
                    for (int i = 0; i < codeMap.length(); i += codeLength) {
                        String code = codeMap.substring(i, i + codeLength);
                        map.put(code, i / codeLength);
                    }

                    for (int i = 0; i < data.length(); i += codeLength) {
                        String code = data.substring(i, i + codeLength).toUpperCase();
                        if (map.containsKey(code)) {
                            sequence.add(map.get(code));
                        } else {
                            throw new Exception("Unknown code found in sequence: " + code);
                        }
                    }
                } else {
                    // variable length code of strings
                    String[] codes = codeMap.toUpperCase().split(",");
                    for (String code : data.split(",")) {
                        boolean isFound = false;
                        for (int iCode = 0; iCode < codes.length - 1; iCode++) {
                            if (code.equals(codes[iCode])) {
                                sequence.add(iCode);
                                isFound = true;
                                break;
                            }
                        }
                        if (!isFound) {
                            throw new Exception("Could not find code " + code + " in codemap");
                        }
                    }
                }
            }
            return sequence;
        } // string2state

        @Override
        public String state2string(List<Integer> nrOfStates) {
            int[] nrOfStates2 = new int[nrOfStates.size()];
            for (int i = 0; i < nrOfStates2.length; i++) {
                nrOfStates2[i] = nrOfStates.get(i);
            }
            return state2string(nrOfStates2);
        }

        /**
         * implementation for single character per state encoding *
         */
        @Override
        public String state2string(int[] nrOfStates) {
            StringBuffer buf = new StringBuffer();
            if (codeMap != null) {
                for (int iState : nrOfStates) {
                    String sCode = codeMap.substring(iState * codeLength, iState * codeLength + codeLength);
                    buf.append(sCode);
                }
            } else {
                // produce a comma separated string of integers
                for (int i = 0; i < nrOfStates.length - 1; i++) {
                    buf.append(nrOfStates[i] + ",");
                }
                buf.append(nrOfStates[nrOfStates.length - 1] + "");
            }
            return buf.toString();
        } // state2string


        @Override
        public int[] getStatesForCode(int iState) {
            return mapCodeToStateSet[iState];
        }

        @Override
        public boolean[] getStateSet(int state) {
            boolean[] stateSet = new boolean[stateCount];
            int[] stateNumbers = getStatesForCode(state);
            for (int i : stateNumbers) {
                stateSet[i] = true;
            }
            return stateSet;
        } // getStateSet

        @Override
        public boolean isAmbiguousState(int state) {
            return (state < 0 && state >= stateCount);
        }

        @Override
        public boolean isStandard() {
            return true;
        }

        @Override
        public char getChar(int state) {
            return (char) (state + 'A');
        }

        @Override
        public String getCode(int state) {
            return String.valueOf(getChar(state));
        }

        @Override
        public String toString() {
            return getDescription();
        }
        
        /** return state associated with a character */
        public Integer char2state(String character) throws Exception {
        	return string2state(character).get(0);
        }
    } // class Base

} // class DataType

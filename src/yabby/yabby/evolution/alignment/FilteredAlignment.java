package yabby.evolution.alignment;

import java.util.Arrays;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.core.parameter.IntegerParameter;
import yabby.evolution.datatype.DataType;


@Description("Alignemnt based on a filter operation on another alignment")
public class FilteredAlignment extends Alignment {
    public Input<String> filterInput = new Input<String>("filter", "specifies which of the sites in the input alignment should be selected " +
            "First site is 1." +
            "Filter specs are comma separated, either a range [from]-[to] or iteration [from]:[to]:[step]; " +
            "1-100 defines a range, " +
            "1-100\3 or 1:100:3 defines every third in range 1-100, " +
            "1::3,2::3 removes every third site. " +
            "Default for range [1]-[last site], default for iterator [1]:[last site]:[1]", Validate.REQUIRED);
    public Input<Alignment> alignmentInput = new Input<Alignment>("data", "alignment to be filtered", Validate.REQUIRED);
    public Input<IntegerParameter> constantSiteWeightsInput = new Input<IntegerParameter>("constantSiteWeights", "if specified, constant " +
    		"sites will be added with weights specified by the input. The dimension and order of weights must match the datatype. " +
    		"For example for nucleotide data, a 4 dimensional " +
    		"parameter with weights for A, C, G and T respectively need to be specified.");

    // these triples specify a range for(i=From; i <= To; i += Step)
    int[] from;
    int[] to;
    int[] step;
    /**
     * list of indices filtered from input alignment *
     */
    int[] filter;
    
    boolean convertDataType = false;

    public FilteredAlignment() {
        sequenceInput.setRule(Validate.OPTIONAL);
    }

    @Override
    public void initAndValidate() throws Exception {
        parseFilterSpec();
        calcFilter();
        Alignment data = alignmentInput.get();
        m_dataType = data.m_dataType;
        // see if this filter changes data type
        if (userDataTypeInput.get() != null) {
            m_dataType = userDataTypeInput.get();
            convertDataType = true;
        }

        if (constantSiteWeightsInput.get() != null) {
        	if (constantSiteWeightsInput.get().getDimension() != m_dataType.getStateCount()) {
        		throw new Exception("constantSiteWeights should be of the same dimension as the datatype " +
        				"(" + constantSiteWeightsInput.get().getDimension() + "!="+ m_dataType.getStateCount() +")");
        	}
    	}
        
        counts = data.counts;
        taxaNames = data.taxaNames;
        stateCounts = data.stateCounts;

        calcPatterns();
    }

    private void parseFilterSpec() throws Exception {
        // parse filter specification
        String filterString = filterInput.get();
        String[] filters = filterString.split(",");
        from = new int[filters.length];
        to = new int[filters.length];
        step = new int[filters.length];
        for (int i = 0; i < filters.length; i++) {
            filterString = " " + filters[i] + " ";
            if (filterString.matches(".*-.*")) {
                // range, e.g. 1-100/3
                if (filterString.indexOf('\\') >= 0) {
                	String str2 = filterString.substring(filterString.indexOf('\\') + 1); 
                	step[i] = parseInt(str2, 1);
                	filterString = filterString.substring(0, filterString.indexOf('\\'));
                } else {
                	step[i] = 1;
                }
                String[] strs = filterString.split("-");
                from[i] = parseInt(strs[0], 1) - 1;
                to[i] = parseInt(strs[1], alignmentInput.get().getSiteCount()) - 1;
            } else if (filterString.matches(".*:.*:.+")) {
                // iterator, e.g. 1:100:3
                String[] strs = filterString.split(":");
                from[i] = parseInt(strs[0], 1) - 1;
                to[i] = parseInt(strs[1], alignmentInput.get().getSiteCount()) - 1;
                step[i] = parseInt(strs[2], 1);
            } else if (filterString.trim().matches("[0-9]*")) {
                from[i] = parseInt(filterString.trim(), 1) - 1;
                to[i] = from[i];
            	step[i] = 1;
            } else {
                throw new Exception("Don't know how to parse filter " + filterString);
            }
        }
    }

    int parseInt(String str, int defaultValue) {
        str = str.replaceAll("\\s+", "");
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void calcFilter() {
        boolean[] isUsed = new boolean[alignmentInput.get().getSiteCount()];
        for (int i = 0; i < to.length; i++) {
            for (int k = from[i]; k <= to[i]; k += step[i]) {
                isUsed[k] = true;
            }
        }
        // count
        int k = 0;
        for (int i = 0; i < isUsed.length; i++) {
            if (isUsed[i]) {
                k++;
            }
        }
        // set up index set
        filter = new int[k];
        k = 0;
        for (int i = 0; i < isUsed.length; i++) {
            if (isUsed[i]) {
                filter[k++] = i;
            }
        }
    }

    @Override
    protected void calcPatterns() {
        int nrOfTaxa = counts.size();
        int nrOfSites = filter.length;
        
        DataType baseType = alignmentInput.get().m_dataType;
        
        
        
        // convert data to transposed int array
        int[][] data = new int[nrOfSites][nrOfTaxa];
        for (int i = 0; i < nrOfTaxa; i++) {
            List<Integer> sites = counts.get(i);
            for (int j = 0; j < nrOfSites; j++) {
                data[j][i] = sites.get(filter[j]);
                if (convertDataType) {
                	try {
                		String code = baseType.getCode(data[j][i]);
						data[j][i] = m_dataType.string2state(code).get(0);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
            }
        }
        
        // add constant sites, if specified
        if (constantSiteWeightsInput.get() != null) {
        	int dim = constantSiteWeightsInput.get().getDimension();
        	// add constant patterns
        	int [][] data2 = new int[nrOfSites + dim][];
            System.arraycopy(data, 0, data2, 0, nrOfSites);
        	for (int i = 0; i < dim; i++) {
        		data2[nrOfSites + i] = new int[nrOfTaxa];
        		for (int j = 0; j < nrOfTaxa; j++) {
        			data2[nrOfSites+ i][j] = i;
				}
        	}
        	data = data2;
        	nrOfSites += dim; 
        }
        
        // sort data
        SiteComparator comparator = new SiteComparator();
        Arrays.sort(data, comparator);

        // count patterns in sorted data
        int[] weights = new int[nrOfSites];
        int nrOfPatterns = 1;
        if (nrOfSites > 0) {
	        weights[0] = 1;
	        for (int i = 1; i < nrOfSites; i++) {
	            if (comparator.compare(data[i - 1], data[i]) != 0) {
	                nrOfPatterns++;
	                data[nrOfPatterns - 1] = data[i];
	            }
	            weights[nrOfPatterns - 1]++;
	        }
        } else {
            nrOfPatterns = 0;
        }
        
        // addjust weight of constant sites, if specified
        if (constantSiteWeightsInput.get() != null) {
        	Integer [] constantWeights = constantSiteWeightsInput.get().getValues(); 
        	for (int i = 0; i < nrOfPatterns; i++) {
        		boolean isContant = true;
        		for (int j = 1; j < nrOfTaxa; j++) {
        			if (data[i][j] != data[i][0]) {
        				isContant = false;
        				break;
        			}
        		}
        		// if this is a constant site, and it is not an ambiguous site
        		if (isContant && data[i][0] >= 0 && data[i][0] < constantWeights.length) {
        			// take weights in data in account as well
        			// by adding constant patterns, we added a weight of 1, which now gets corrected
            		weights[i] = weights[i] - 1 + constantWeights[data[i][0]];
        		}
        	}
        	
        	// need to decrease nSites for mapping sites to patterns in m_nPatternIndex
        	nrOfSites -= constantWeights.length; 
        }        
        
        // reserve memory for patterns
        patternWeight = new int[nrOfPatterns];
        sitePatterns = new int[nrOfPatterns][nrOfTaxa];
        for (int i = 0; i < nrOfPatterns; i++) {
            patternWeight[i] = weights[i];
            sitePatterns[i] = data[i];
        }

        // find patterns for the sites
        patternIndex = new int[nrOfSites];
        for (int i = 0; i < nrOfSites; i++) {
            int[] sites = new int[nrOfTaxa];
            for (int j = 0; j < nrOfTaxa; j++) {
                sites[j] = counts.get(j).get(filter[i]);
                if (convertDataType) {
                	try {
                		sites[j] = m_dataType.string2state(baseType.getCode(sites[j])).get(0);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
            }
            patternIndex[i] = Arrays.binarySearch(sitePatterns, sites, comparator);
        }

        // determine maximum state count
        // Usually, the state count is equal for all sites,
        // though for SnAP analysis, this is typically not the case.
        maxStateCount = 0;
        for (int stateCount1 : stateCounts) {
            maxStateCount = Math.max(maxStateCount, stateCount1);
        }
        if (convertDataType) {
        	maxStateCount = Math.max(maxStateCount, m_dataType.getStateCount());
        }
        // report some statistics
        //for (int i = 0; i < m_sTaxaNames.size(); i++) {
        //    System.err.println(m_sTaxaNames.get(i) + ": " + m_counts.get(i).size() + " " + m_nStateCounts.get(i));
        //}
        System.err.println("Filter " + filterInput.get());
        System.err.println(getNrTaxa() + " taxa");
        if (constantSiteWeightsInput.get() != null) {
        	Integer [] constantWeights = constantSiteWeightsInput.get().getValues();
        	int sum = 0; 
        	for (int i : constantWeights) { 
        		sum += i;
        	}
        	System.err.println(getSiteCount() + " sites + " + sum + " constant sites");
        } else {
        	System.err.println(getSiteCount() + " sites");
        }
        System.err.println(getPatternCount() + " patterns");
    }
    
    /** return indices of the sites that the filter uses **/
    public int [] indices() {
    	return filter.clone();
    }
}

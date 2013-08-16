package yabby.evolution.datatype;

import java.util.List;

/** represent data associated with a set of taxa **/
public interface Data {
	public List<String> getTaxaNames();
    public int getNrTaxa();

    public List<Integer> getStateCounts();
    public List<List<Integer>> getCounts();
    public DataType getDataType();
    public int getPatternCount();
    public int[] getPattern(int id);
    public int getPattern(int iTaxon, int id);
    public int getPatternWeight(int id);
    public int getMaxStateCount();
    public int getPatternIndex(int iSite);
    public int getSiteCount();
    public int [] getWeights();
    
}

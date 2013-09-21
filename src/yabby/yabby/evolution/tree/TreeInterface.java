package yabby.evolution.tree;

import yabby.evolution.alignment.TaxonSet;

public interface TreeInterface {
	int getLeafNodeCount();
	int getInternalNodeCount();
	int getNodeCount();

	Node getRoot();
    Node [] getNodesAsArray();
    Node getNode(int i);
    
    String getID();

    //String [] getTaxaNames();
    TaxonSet getTaxonset();
    
	boolean somethingIsDirty();

    public void getMetaData(Node node, Double[] fT, String sPattern);
    public void setMetaData(Node node, Double[] fT, String sPattern);

}

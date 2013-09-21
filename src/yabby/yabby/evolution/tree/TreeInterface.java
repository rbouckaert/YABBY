package yabby.evolution.tree;

import yabby.evolution.alignment.TaxonSet;

public interface TreeInterface {
    String getID();

    int getLeafNodeCount();
	int getInternalNodeCount();
	int getNodeCount();

	Node getRoot();
    Node getNode(int i);
    Node [] getNodesAsArray();

    TaxonSet getTaxonset();
    
	boolean somethingIsDirty();

    public void getMetaData(Node node, Double[] fT, String sPattern);
    public void setMetaData(Node node, Double[] fT, String sPattern);

}

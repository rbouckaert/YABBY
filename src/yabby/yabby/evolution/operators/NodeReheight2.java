package yabby.evolution.operators;

import yabby.core.Description;
import yabby.core.Input;
import yabby.evolution.tree.Node;
import yabby.evolution.tree.Tree;
import yabby.util.Randomizer;

@Description("Tree operator which randomly changes the height of a node, " +
		"then reconstructs the tree from node heights. " +
		"Works on single trees (no gene trees required) and uses scaling to determine new node height.")
public class NodeReheight2 extends TreeOperator {
    public Input<Double> m_pScaleFactor = new Input<Double>("scaleFactor", "scaling factor: larger means more bold proposals", 1.0);
    /**  shadows input **/
    double m_fScaleFactor;
	Node[] m_nodes;
	
	@Override
	public void initAndValidate() throws Exception {
        m_fScaleFactor = m_pScaleFactor.get();
	}
	
	@Override
	public double proposal() {
		Tree tree = treeInput.get();
		m_nodes = tree.getNodesAsArray();
		int nNodes = tree.getNodeCount();
		// randomly change left/right order
		reorder(tree.getRoot());
		// collect heights
		double [] fHeights = new double[nNodes];
		int [] iReverseOrder = new int[nNodes];
		collectHeights(tree.getRoot(), fHeights, iReverseOrder, 0);
		// change height of an internal node
		int iNode = Randomizer.nextInt(fHeights.length);
		while (m_nodes[iReverseOrder[iNode]].isLeaf()) {
			iNode = Randomizer.nextInt(fHeights.length);
		}

		double fScale =  (m_fScaleFactor + (Randomizer.nextDouble() * ((1.0 / m_fScaleFactor) - m_fScaleFactor)));
		fHeights[iNode] *= fScale;

		m_nodes[iReverseOrder[iNode]].setHeight(fHeights[iNode]);
		// reconstruct tree from heights
		Node root = reconstructTree(fHeights, iReverseOrder, 0, fHeights.length, new boolean[fHeights.length]);
		
		if (!checkConsistency(root, new boolean[fHeights.length])) {
			System.err.println("Inconsisten tree");
		}
		root.setParent(null);
		tree.setRoot(root);
		return -Math.log(fScale);
	}

	private boolean checkConsistency(Node node, boolean[] bUsed) {
		if (bUsed[node.getNr()]) {
			return false;
		}
		bUsed[node.getNr()] = true;
		if (!node.isLeaf()) {
			return checkConsistency(node.getLeft(), bUsed) && checkConsistency(node.getRight(), bUsed);
		}
		return true;
	}

	

	/** construct tree top down by joining highest left and right nodes **/
	private Node reconstructTree(double[] fHeights, int [] iReverseOrder, int iFrom, int iTo, boolean [] bHasParent) {
		//iNode = maxIndex(fHeights, 0, fHeights.length);
		int iNode = -1;
		double fMax = Double.NEGATIVE_INFINITY;
		for (int j = iFrom; j < iTo; j++) {
			if (fMax < fHeights[j] && !m_nodes[iReverseOrder[j]].isLeaf()) {
				fMax = fHeights[j];
				iNode = j;
			}
		}
		if (iNode < 0) {
			return null;
		}
		Node node = m_nodes[iReverseOrder[iNode]];

		//int iLeft = maxIndex(fHeights, 0, iNode);
		int iLeft = -1;
		fMax = Double.NEGATIVE_INFINITY;
		for (int j = iFrom; j < iNode; j++) {
			if (fMax < fHeights[j] && !bHasParent[j]) {
				fMax = fHeights[j];
				iLeft = j;
			}
		}
		
		//int iRight = maxIndex(fHeights, iNode+1, fHeights.length);
		int iRight = -1;
		fMax = Double.NEGATIVE_INFINITY;
		for (int j = iNode+1; j < iTo; j++) {
			if (fMax < fHeights[j] && !bHasParent[j]) {
				fMax = fHeights[j];
				iRight = j;
			}
		}

		node.setLeft(m_nodes[iReverseOrder[iLeft]]);
		node.getLeft().setParent(node);
		node.setRight(m_nodes[iReverseOrder[iRight]]);
		node.getRight().setParent(node);
		if (node.getLeft().isLeaf()) {
			fHeights[iLeft] = Double.NEGATIVE_INFINITY;
		}
		if (node.getRight().isLeaf()) {
			fHeights[iRight] = Double.NEGATIVE_INFINITY;
		}
		bHasParent[iLeft] = true;
		bHasParent[iRight] = true;
		fHeights[iNode] = Double.NEGATIVE_INFINITY;

		
		reconstructTree(fHeights, iReverseOrder, iFrom, iNode, bHasParent);
		reconstructTree(fHeights, iReverseOrder, iNode, iTo, bHasParent);
		return node;
	}

	/** gather height of each node, and order of the nodes **/
	private int collectHeights(Node node, double[] fHeights, int [] iReverseOrder, int iCurrent) {
		if (node.isLeaf()) {
			fHeights[iCurrent] = node.getHeight();
			iReverseOrder[iCurrent] = node.getNr();
			iCurrent++;
		} else {
			iCurrent = collectHeights(node.getLeft(), fHeights, iReverseOrder, iCurrent);
			fHeights[iCurrent] = node.getHeight();
			iReverseOrder[iCurrent] = node.getNr();
			iCurrent++;
			iCurrent = collectHeights(node.getRight(), fHeights, iReverseOrder, iCurrent);
		}
		return iCurrent;
	}

	/** randomly changes left and right children in every internal node **/
	private void reorder(Node node) {
		if (!node.isLeaf()) {
			if (Randomizer.nextBoolean()) {
				Node tmp = node.getLeft();
				node.setLeft(node.getRight());
				node.setRight(tmp);
			}
			reorder(node.getLeft());
			reorder(node.getRight());
		}
	}

    @Override
    public void optimize(double logAlpha) {
        double fDelta = calcDelta(logAlpha);
        fDelta += Math.log(1.0 / m_fScaleFactor - 1.0);
        m_fScaleFactor = 1.0 / (Math.exp(fDelta) + 1.0);
    }

} // class NodeReheight

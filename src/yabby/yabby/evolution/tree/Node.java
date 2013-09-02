/*
* File Node.java
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
package yabby.evolution.tree;


import java.util.*;

import yabby.core.Description;
import yabby.core.YABBYObject;
import yabby.util.HeapSort;

@Description("Nodes in building beast.tree data structure.")
public class Node extends YABBYObject {

    /**
     * label nr of node, used mostly when this is a leaf.
     */
    protected int m_iLabel;

    /**
     * height of this node.
     */
    protected double m_fHeight = Double.MAX_VALUE;

    /**
     * Arbitrarily labeled metadata on this node. Not currently implemented as part of state!
     */
    protected Map<String, Object> metaData;

    /**
     * list of children of this node *
     * Don't use m_left and m_right directly
     * Use getChildCount() and getChild(x) or getChildren() instead
     */
    List<Node> children = new ArrayList<Node>();

//    @Deprecated
//	private Node m_left;
//    @Deprecated
//	private Node m_right;

    /**
     * parent node in the beast.tree, null if root *
     */
    Node m_Parent = null;

    /**
     * status of this node after an operation is performed on the state *
     */
    int m_bIsDirty = Tree.IS_CLEAN;

    /**
     * meta-data contained in square brackets in Newick *
     */
    public String m_sMetaData;

    /**
     * The Tree that this node is a part of.
     * This allows e.g. access to the State containing the Tree *
     */
    protected Tree m_tree;

    public Node() {
    }

    public Node(String id) throws Exception {
        setID(id);
        initAndValidate();
    }

    public Tree getTree() {
        return m_tree;
    }

    @Override
    public void initAndValidate() throws Exception {
        // do nothing
    }

    /**
     * @return number uniquely identifying the node in the tree.
     *         This is a number between 0 and the total number of nodes in the tree
     *         Leaf nodes are number 0 to #leaf nodes -1
     *         Internal nodes are numbered  #leaf nodes  up to #nodes-1
     *         The root node is not guaranteed a number.
     *         Node number is guaranteed not to change during an MCMC run.
     */
    public int getNr() {
        return m_iLabel;
    }

    public void setNr(int iLabel) {
        m_iLabel = iLabel;
    }

    public double getHeight() {
        return m_fHeight;
    }

    public double getDate() {
        return m_tree.getDate(m_fHeight);
    }

    public void setHeight(double fHeight) {
        startEditing();
        m_fHeight = fHeight;
        m_bIsDirty |= Tree.IS_DIRTY;
        if (!isLeaf()) {
            getLeft().m_bIsDirty |= Tree.IS_DIRTY;
            if (getRight() != null) {
                getRight().m_bIsDirty |= Tree.IS_DIRTY;
            }
        }
    }

    /**
     * @return length of branch in the beast.tree *
     */
    public final double getLength() {
        if (isRoot()) {
            return 0;
        } else {
            return getParent().m_fHeight - m_fHeight;
        }
    }

    /**
     * methods for accessing the dirtiness state of the Node.
     * A Node is Tree.IS_DIRTY if its value (like height) has changed
     * A Node Tree.IS_if FILTHY if its parent or child has changed.
     * Otherwise the node is Tree.IS_CLEAN *
     */
    public int isDirty() {
        return m_bIsDirty;
    }

    public void makeDirty(int nDirty) {
        m_bIsDirty |= nDirty;
    }

    public void makeAllDirty(int nDirty) {
        m_bIsDirty = nDirty;
        if (!isLeaf()) {
            getLeft().makeAllDirty(nDirty);
            if (getRight() != null) {
                getRight().makeAllDirty(nDirty);
            }
        }
    }


    /**
     * @return parent node, or null if this is root *
     */
    public Node getParent() {
        return m_Parent;
    }

    /**
     * Calls setParent(parent, true)
     *
     * @param parent the new parent to be set, must be called from within an operator.
     */
    public void setParent(Node parent) {
        setParent(parent, true);
    }

    /**
     * Sets the parent of this node
     *
     * @param parent     the node to become parent
     * @param inOperator if true, then startEditing() is called and setting the parent will make tree "filthy"
     */
    void setParent(Node parent, boolean inOperator) {
        if (inOperator) startEditing();
        if (m_Parent != parent) {
            m_Parent = parent;
            if (inOperator) m_bIsDirty = Tree.IS_FILTHY;
        }
    }

    /**
     * @return a copy of a list of immediate child nodes of this node.
     *         Note that changing the list does not affect the topology of the tree.
     */
    public List<Node> getChildren() {
        List<Node> copyOfChildren = new ArrayList<Node>();
        copyOfChildren.addAll(children);
        return copyOfChildren;
    }

    /**
     * get all child node under this node, if this node is leaf then list.size() = 0.
     *
     * @return
     */
    public List<Node> getAllChildNodes() {
        List<Node> childNodes = new ArrayList<Node>();
        if (!this.isLeaf()) getAllChildNodes(childNodes);
        return childNodes;
    }

    // recursive
    public void getAllChildNodes(List<Node> childNodes) {
        childNodes.add(this);
        if (!this.isLeaf()) {
            getRight().getAllChildNodes(childNodes);
            getLeft().getAllChildNodes(childNodes);
        }
    }

    /**
     * get all leaf node under this node, if this node is leaf then list.size() = 0.
     *
     * @return
     */
    public List<Node> getAllLeafNodes() {
        List<Node> leafNodes = new ArrayList<Node>();
        if (!this.isLeaf()) getAllLeafNodes(leafNodes);
        return leafNodes;
    }

    // recursive
    public void getAllLeafNodes(List<Node> leafNodes) {
        if (this.isLeaf()) {
            leafNodes.add(this);
        } else {
            getRight().getAllLeafNodes(leafNodes);
            getLeft().getAllLeafNodes(leafNodes);
        }
    }

    /**
     * @return true if current node is root node *
     */
    public boolean isRoot() {
        return m_Parent == null;
    }

    /**
     * @return true if current node is a leaf node *
     */
    public boolean isLeaf() {
        return children.size() == 0;
        //return getLeft() == null && getRight() == null;
    }

    public void removeChild(Node child) {
        startEditing();
        children.remove(child);
    }

    /**
     * Removes all children from this node.
     *
     * @param inOperator if true then startEditing() is called. For operator uses, called removeAllChildren(true), otherwise
     *                   use set to false.
     */
    public void removeAllChildren(boolean inOperator) {
        if (inOperator) startEditing();
        children.clear();
    }

    public void addChild(Node child) {
        child.setParent(this);
        children.add(child);
    }

    /**
     * @return count number of nodes in beast.tree, starting with current node *
     */
    public int getNodeCount() {
        int nodes = 1;
        for (Node child : children) {
            nodes += child.getNodeCount();
        }
        return nodes;
    }

    public int getLeafNodeCount() {
        if (isLeaf()) {
            return 1;
        }
        int nodes = 0;
        for (Node child : children) {
            nodes += child.getLeafNodeCount();
        }
        return nodes;
    }

    public int getInternalNodeCount() {
        if (isLeaf()) {
            return 0;
        }
        int nodes = 1;
        for (Node child : children) {
            nodes += child.getInternalNodeCount();
        }
        return nodes;
    }

    /**
     * @return beast.tree in Newick format, with length and meta data
     *         information. Unlike toNewick(), here Nodes are numbered, instead of
     *         using the node labels.
     *         If there are internal nodes with non-null IDs then their numbers are also printed.
     *         Also, all internal nodes are labelled if bPrintInternalNodeNumbers
     *         is set true. This is useful for example when storing a State to file
     *         so that it can be restored.
     */
    public String toShortNewick(boolean bPrintInternalNodeNumbers) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toShortNewick(bPrintInternalNodeNumbers));
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toShortNewick(bPrintInternalNodeNumbers));
            }
            buf.append(")");
            if (getID() != null) {
                buf.append(getNr());
            } else if (bPrintInternalNodeNumbers) {
                buf.append(getNr());
            }

        } else {
            buf.append(getNr());
        }
        buf.append(getNewickMetaData());
        buf.append(":").append(getLength());
        return buf.toString();
    }

    /**
     * prints newick string where it orders by highest leaf number
     * in a clade. Print node numbers (m_iLabel) incremented by 1
     * for leaves and internal nodes with non-null IDs.
     */
    String toSortedNewick(int[] iMaxNodeInClade) {
        return toSortedNewick(iMaxNodeInClade, false);
    }

    public String toSortedNewick(int[] iMaxNodeInClade, boolean printMetaData) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            String sChild1 = getLeft().toSortedNewick(iMaxNodeInClade, printMetaData);
            int iChild1 = iMaxNodeInClade[0];
            if (getRight() != null) {
                String sChild2 = getRight().toSortedNewick(iMaxNodeInClade, printMetaData);
                int iChild2 = iMaxNodeInClade[0];
                if (iChild1 > iChild2) {
                    buf.append(sChild2);
                    buf.append(",");
                    buf.append(sChild1);
                } else {
                    buf.append(sChild1);
                    buf.append(",");
                    buf.append(sChild2);
                    iMaxNodeInClade[0] = iChild1;
                }
            } else {
                buf.append(sChild1);
            }
            buf.append(")");
            if (getID() != null) {
                buf.append(m_iLabel+1);
            }
        } else {
            iMaxNodeInClade[0] = m_iLabel;
            buf.append(m_iLabel + 1);
        }

        if (printMetaData) {
            buf.append(getNewickMetaData());
        }
        buf.append(":").append(getLength());
        return buf.toString();
    }

    @Deprecated
    public String toNewick(List<String> labels) {
        throw new UnsupportedOperationException("Please use toNewick(). Labels will come from node.getId() or node.getNr().");
    }


        /**
         * @return beast.tree in Newick format with taxon labels for labelled tip nodes
         * and labeled (having non-null ID) internal nodes.
         * If a tip node doesn't have an ID (taxon label) then node number (m_iLabel) is printed.
         */
    public String toNewick() {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toNewick());
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toNewick());
            }
            buf.append(")");
            if (getID() != null) {
                buf.append(getID());
            }
        } else {
            if (getID() == null) {
                buf.append(m_iLabel);
            } else {
                buf.append(getID());
            }
        }
        buf.append(getNewickMetaData());
        buf.append(":").append(getLength());
        return buf.toString();
    }

    public String getNewickMetaData() {
        if (m_sMetaData != null) {
            return "[&" + m_sMetaData + ']';
        }
        return "";
    }

    /**
     * @param sLabels
     * @return beast.tree in long Newick format, with all length and meta data
     *         information, but with leafs labelled with their names
     */
    public String toString(List<String> sLabels) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toString(sLabels));
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toString(sLabels));
            }
            buf.append(")");
        } else {
            buf.append(sLabels.get(m_iLabel));
        }
        if (m_sMetaData != null) {
            buf.append('[');
            buf.append(m_sMetaData);
            buf.append(']');
        }
        buf.append(":").append(getLength());
        return buf.toString();
    }

    public String toString() {
        return toShortNewick(true);
    }

    /**
     * sorts nodes in children according to lowest numbered label in subtree
     *
     * @return
     */
    public int sort() {

        if (isLeaf()) {
            return m_iLabel;
        }

        int childCount = getChildCount();

        if (childCount == 1) return getChild(0).sort();

        List<Integer> lowest = new ArrayList<Integer>();
        int[] indices = new int[childCount];

        // relies on this being a copy of children list
        List<Node> children = getChildren();

        for (Node child : children) {
            lowest.add(child.sort());
        }
        HeapSort.sort(lowest, indices);
        for (int i = 0; i < childCount; i++) {
            setChild(i, children.get(indices[i]));
        }
        return lowest.get(indices[0]);
    } // sort

    /**
     * during parsing, leaf nodes are numbered 0...m_nNrOfLabels-1
     * but internal nodes are left to zero. After labeling internal
     * nodes, m_iLabel uniquely identifies a node in a beast.tree.
     *
     * @param iLabel
     * @return
     */
    public int labelInternalNodes(int iLabel) {
        if (isLeaf()) {
            return iLabel;
        } else {
            iLabel = getLeft().labelInternalNodes(iLabel);
            if (getRight() != null) {
                iLabel = getRight().labelInternalNodes(iLabel);
            }
            m_iLabel = iLabel++;
        }
        return iLabel;
    } // labelInternalNodes

    /**
     * @return (deep) copy of node
     */
    public Node copy() {
        Node node = new Node();
        node.m_fHeight = m_fHeight;
        node.m_iLabel = m_iLabel;
        node.m_sMetaData = m_sMetaData;
        node.m_Parent = null;
        node.setID(ID);

        for (Node child : getChildren()) {
            node.addChild(child.copy());
        }
        return node;
    } // copy

    /**
     * assign values to a tree in array representation *
     */
    public void assignTo(Node[] nodes) {
        Node node = nodes[getNr()];
        node.m_fHeight = m_fHeight;
        node.m_iLabel = m_iLabel;
        node.m_sMetaData = m_sMetaData;
        node.m_Parent = null;
        node.ID = ID;
        if (getLeft() != null) {
            node.setLeft(nodes[getLeft().getNr()]);
            getLeft().assignTo(nodes);
            node.getLeft().m_Parent = node;
            if (getRight() != null) {
                node.setRight(nodes[getRight().getNr()]);
                getRight().assignTo(nodes);
                node.getRight().m_Parent = node;
            }
        }
    }

    /**
     * assign values from a tree in array representation *
     */
    public void assignFrom(Node[] nodes, Node node) {
        m_fHeight = node.m_fHeight;
        m_iLabel = node.m_iLabel;
        m_sMetaData = node.m_sMetaData;
        m_Parent = null;
        ID = node.ID;
        if (node.getLeft() != null) {
            setLeft(nodes[node.getLeft().getNr()]);
            getLeft().assignFrom(nodes, node.getLeft());
            getLeft().m_Parent = this;
            if (node.getRight() != null) {
                setRight(nodes[node.getRight().getNr()]);
                getRight().assignFrom(nodes, node.getRight());
                getRight().m_Parent = this;
            }
        }
    }

    /**
     * set meta-data according to pattern.
     * Only heights are recognised, but derived classes could deal with
     * richer meta data pattersn.
     */
    public void setMetaData(String sPattern, Object fValue) {
        startEditing();
        if (sPattern.equals(TraitSet.DATE_TRAIT) ||
                sPattern.equals(TraitSet.DATE_FORWARD_TRAIT) ||
                sPattern.equals(TraitSet.DATE_BACKWARD_TRAIT)) {
            m_fHeight = (Double) fValue;
            m_bIsDirty |= Tree.IS_DIRTY;
        } else {
            if (metaData == null) metaData = new TreeMap<String, Object>();
            metaData.put(sPattern, fValue);
        }

    }

    public Object getMetaData(String sPattern) {
        if (sPattern.equals(TraitSet.DATE_TRAIT) ||
                sPattern.equals(TraitSet.DATE_FORWARD_TRAIT) ||
                sPattern.equals(TraitSet.DATE_BACKWARD_TRAIT)) {
            return m_fHeight;
        } else if (metaData != null) {
            Object d = metaData.get(sPattern);
            if (d != null) return d;
        }
        return 0;
    }

    public Set<String> getMetaDataNames() {
        if (metaData == null) {
            return null;
        }
        return metaData.keySet();
    }


    /**
     * scale height of this node and all its descendants
     *
     * @param fScale scale factor
     */
    public void scale(double fScale) throws Exception {
        startEditing();
        m_bIsDirty |= Tree.IS_DIRTY;
        if (!isLeaf()) {
            m_fHeight *= fScale;
            getLeft().scale(fScale);
            if (getRight() != null) {
                getRight().scale(fScale);
            }
            if (m_fHeight < getLeft().m_fHeight || m_fHeight < getRight().m_fHeight) {
                throw new Exception("Scale gives negative branch length");
            }
        }
    }

    protected void startEditing() {
        if (m_tree != null && m_tree.getState() != null) {
            m_tree.startEditing(null);
        }
    }

    /**
     * some methods that are useful for porting from BEAST 1 *
     */
    public int getChildCount() {
        return children.size();
    }

    public Node getChild(int iChild) {
        return children.get(iChild);
    }

    public void setChild(int iChild, Node node) {
        while (children.size() < iChild) {
            children.add(null);
        }
        children.set(iChild, node);
    }


    public void setLeft(Node m_left) {
        if (children.size() == 0) {
            children.add(m_left);
        } else {
            children.set(0, m_left);
        }
    }

    public Node getLeft() {
        if (children.size() == 0) {
            return null;
        }
        return children.get(0);
    }

    public void setRight(Node m_right) {
        switch (children.size()) {
            case 0:
                children.add(null);
            case 1:
                children.add(m_right);
                break;
            default:
                children.set(1, m_right);
                break;
        }
    }

    public Node getRight() {
        if (children.size() <= 1) {
            return null;
        }
        return children.get(1);
    }

    public static Node connect(Node left, Node right, double h) {
        Node n = new Node();
        n.setHeight(h);
        n.setLeft(left);
        n.setRight(right);
        return n;
    }

    /** 
     * is true if this leaf actually represents a direct ancestor (i.e. is on the end of a zero-length branch) 
     */
	public boolean isDirectAncestor() {
		return (!isRoot() && this.getParent().getHeight() == this.getHeight());
	}
	
} // class Node

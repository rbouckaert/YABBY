package test.yabby.core;

import org.junit.Test;



import test.yabby.BEASTTestCase;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.tree.Tree.BaseTree;
import yabby.util.ClusterTree;
import yabby.util.TreeParser;

import junit.framework.TestCase;

public class StateNodeInitialiserTest extends TestCase {

    @Test
    public void testClusterTree() throws Exception {
        Alignment data = BEASTTestCase.getAlignment();
        BaseTree tree = new BaseTree();
        tree.initAndValidate();
        assertEquals(true, tree.getNodeCount() == 1);

        TreeParser tree2 = new TreeParser();
        tree2.initByName(
                "initial", tree,
                "taxa", data,
                "newick", "((((human:0.024003,(chimp:0.010772,bonobo:0.010772):0.013231):0.012035,gorilla:0.036038):0.033087000000000005,orangutan:0.069125):0.030456999999999998,siamang:0.099582);");

        assertEquals(true, tree.getNodeCount() > 1);
        assertEquals(11, tree.getNodeCount());
    }

    @Test
    public void testNewickTree() throws Exception {
        Alignment data = BEASTTestCase.getAlignment();
        BaseTree tree = new BaseTree();
        tree.initAndValidate();
        assertEquals(true, tree.getNodeCount() == 1);

        ClusterTree tree2 = new ClusterTree();
        tree2.initByName(
                "initial", tree,
                "clusterType", "upgma",
                "taxa", data);
        assertEquals(true, tree.getNodeCount() > 1);
        assertEquals(11, tree.getNodeCount());
    }
}

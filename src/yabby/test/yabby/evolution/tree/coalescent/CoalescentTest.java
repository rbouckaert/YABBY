package test.yabby.evolution.tree.coalescent;

import test.yabby.BEASTTestCase;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.tree.Tree.BaseTree;
import yabby.evolution.tree.coalescent.Coalescent;
import yabby.evolution.tree.coalescent.ConstantPopulation;
import yabby.evolution.tree.coalescent.TreeIntervals;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class CoalescentTest extends BEASTTestCase {
    String[] trees = new String[]{"(((A:1.0,B:1.0):1.0,C:2.0);", ""}; //more trees ?
    Alignment data;
    final double pop = 10000;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        data = getFourTaxaNoData();
    }

    public void testConstantPopulation() throws Exception {
        // *********** 3 taxon **********
        BaseTree tree = getTree(data, trees[0]);
        TreeIntervals treeIntervals = new TreeIntervals();
        treeIntervals.initByName("tree", tree);

        ConstantPopulation cp = new ConstantPopulation();
        cp.initByName("popSize", Double.toString(pop));

        Coalescent coal = new Coalescent();
        coal.initByName("treeIntervals", treeIntervals, "populationModel", cp);

        double logL = coal.calculateLogP();

        assertEquals(logL, -(4 / pop) - 2 * Math.log(pop), PRECISION);

        // *********** 4 taxon **********
//        tree = getTree(data, trees[1]);
//        treeIntervals = new TreeIntervals();
//        treeIntervals.initByName("tree", tree);
//
//        cp = new ConstantPopulation();
//        cp.initByName("popSize", Double.toString(pop));
//
//        coal = new Coalescent();
//        coal.initByName("treeIntervals", treeIntervals, "populationModel", cp);
//
//        logL = coal.calculateLogP();
//
//        assertEquals(logL, -(4 / pop) - 2 * Math.log(pop), PRECISION);

    }

    public void testExponentialGrowth() throws Exception {

    }

}

package test.yabby.evolution.speciation;

import org.junit.Test;


import test.yabby.BEASTTestCase;
import yabby.core.parameter.RealParameter;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.speciation.BirthDeathGernhard08Model;
import yabby.evolution.tree.Tree.BaseTree;
import junit.framework.TestCase;

public class BirthDeathGernhard08ModelTest extends TestCase {


    @Test
    public void testJC69Likelihood() throws Exception {
        // Set up JC69 model: uniform freqs, kappa = 1, 0 gamma categories
        Alignment data = BEASTTestCase.getAlignment();
        BaseTree tree = BEASTTestCase.getTree(data);

        RealParameter birthDiffRate = new RealParameter("1.0");
        RealParameter relativeDeathRate = new RealParameter("0.5");
        BirthDeathGernhard08Model likelihood = new BirthDeathGernhard08Model();
        likelihood.initByName("type", "unscaled",
                "tree", tree,
                "birthDiffRate", birthDiffRate,
                "relativeDeathRate", relativeDeathRate);


        double fLogP = 0;
        fLogP = likelihood.calculateLogP(); // -3.520936119641363
        assertEquals(fLogP, 2.5878899503981287, BEASTTestCase.PRECISION);

        likelihood.initByName("type", "timesonly",
                "tree", tree,
                "birthDiffRate", birthDiffRate,
                "relativeDeathRate", relativeDeathRate);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, 9.16714116240823, BEASTTestCase.PRECISION);

        likelihood.initByName("type", "oriented",
                "tree", tree,
                "birthDiffRate", birthDiffRate,
                "relativeDeathRate", relativeDeathRate);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, 4.379649419626184, BEASTTestCase.PRECISION);

        likelihood.initByName("type", "labeled",
                "tree", tree,
                "birthDiffRate", birthDiffRate,
                "relativeDeathRate", relativeDeathRate);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, 1.2661341104158121, BEASTTestCase.PRECISION);
    }

}

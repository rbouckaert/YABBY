package test.yabby.evolution.likelihood;


import junit.framework.TestCase;
import org.junit.Test;

import test.yabby.BEASTTestCase;
import yabby.core.parameter.RealParameter;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.alignment.Sequence;
import yabby.evolution.datatype.UserDataType;
import yabby.evolution.likelihood.TreeLikelihood;
import yabby.evolution.sitemodel.SiteModel;
import yabby.evolution.substitutionmodel.BinaryCovarion;
import yabby.evolution.substitutionmodel.Blosum62;
import yabby.evolution.substitutionmodel.CPREV;
import yabby.evolution.substitutionmodel.Dayhoff;
import yabby.evolution.substitutionmodel.Frequencies;
import yabby.evolution.substitutionmodel.GTR;
import yabby.evolution.substitutionmodel.GeneralSubstitutionModel;
import yabby.evolution.substitutionmodel.HKY;
import yabby.evolution.substitutionmodel.JTT;
import yabby.evolution.substitutionmodel.JukesCantor;
import yabby.evolution.substitutionmodel.MTREV;
import yabby.evolution.substitutionmodel.MutationDeathModel;
import yabby.evolution.substitutionmodel.SubstitutionModel;
import yabby.evolution.substitutionmodel.WAG;
import yabby.evolution.tree.Tree;

/**
 * This test mimics the testLikelihood.xml file from Beast 1, which compares Beast 1 results to PAUP results.
 * So, it these tests succeed, then Beast II calculates the same for these simple models as Beast 1 and PAUP.
 * *
 */
public class TreeLikelihoodTest extends TestCase {

    public TreeLikelihoodTest() {
        super();
    }

    protected TreeLikelihood newTreeLikelihood() {
    	System.setProperty("java.only","true");
        return new TreeLikelihood();
    }

    @Test
    public void testJC69Likelihood() throws Exception {
        // Set up JC69 model: uniform freqs, kappa = 1, 0 gamma categories
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        JukesCantor JC = new JukesCantor();
        JC.initAndValidate();

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", JC);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);
        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1992.2056440317247, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1992.2056440317247, BEASTTestCase.PRECISION);
    }

    @Test
    public void testAscertainedJC69Likelihood() throws Exception {
        // as testJC69Likelihood but with ascertained alignment
        Alignment data = BEASTTestCase.getAscertainedAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data,
                "estimate", false);

        HKY hky = new HKY();
        hky.initByName("kappa", "1.0", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        // the following number comes from Beast 1.6
        assertEquals(fLogP, -737.7140695360017, BEASTTestCase.PRECISION);
    }

    @Test
    public void testK80Likelihood() throws Exception {
        // Set up K80 model: uniform freqs, kappa = 27.402591, 0 gamma categories
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data,
                "estimate", false);

        HKY hky = new HKY();
        hky.initByName("kappa", "27.40259", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1856.303048876734, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1856.303048876734, BEASTTestCase.PRECISION);
    }

    @Test
    public void testHKY85Likelihood() throws Exception {
        // Set up HKY85 model: estimated freqs, kappa = 29.739445, 0 gamma categories
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        HKY hky = new HKY();
        hky.initByName("kappa", "29.739445", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1825.2131708068507, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1825.2131708068507, BEASTTestCase.PRECISION);
    }


    @Test
    public void testHKY85GLikelihood() throws Exception {
        // Set up HKY85+G model: estimated freqs, kappa = 38.82974, 4 gamma categories, shape = 0.137064
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        HKY hky = new HKY();
        hky.initByName("kappa", "38.82974", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 4,
                "shape", "0.137064",
                "proportionInvariant", "0.0",
                "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        System.err.println(fLogP - -1789.7593576610134);
        assertEquals(fLogP, -1789.7593576610134, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1789.7593576610134, BEASTTestCase.PRECISION);
    }

    @Test
    public void testHKY85ILikelihood() throws Exception {
        // Set up HKY85+I model: estimated freqs, kappa = 38.564672, 0 gamma categories, prop invariant = 0.701211
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        HKY hky = new HKY();
        hky.initByName("kappa", "38.564672", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1,
                "shape", "0.137064",
                "proportionInvariant", "0.701211",
                "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1789.912401996943, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1789.912401996943, BEASTTestCase.PRECISION);
    }

    @Test
    public void testHKY85GILikelihood() throws Exception {
        // Set up HKY85+G+I model: estimated freqs, kappa = 39.464538, 4 gamma categories, shape = 0.587649, prop invariant = 0.486548
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        HKY hky = new HKY();
        hky.initByName("kappa", "39.464538", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 4,
                "shape", "0.587649",
                "proportionInvariant", "0.486548",
                "substModel", hky);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1789.639227747059, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1789.639227747059, BEASTTestCase.PRECISION);
    }


    @Test
    public void testGTRLikelihood() throws Exception {
        // Set up GTR model: no gamma categories, no proportion invariant
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        GTR gsm = new GTR();
        gsm.initByName("frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1,
                "substModel", gsm);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1969.145839307625, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", false, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1969.145839307625, BEASTTestCase.PRECISION);
    }

    @Test
    public void testGTRILikelihood() throws Exception {
        // Set up GTR model: prop invariant = 0.5
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        GeneralSubstitutionModel gsm = new GeneralSubstitutionModel();
        gsm.initByName("rates", "1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1,
                "proportionInvariant", "0.5",
                "substModel", gsm);
        //siteModel.init("1.0", 1, null, "0.5", gsm);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1948.8417455357564, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", false, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1948.8417455357564, BEASTTestCase.PRECISION);
    }

    @Test
    public void testGTRGLikelihood() throws Exception {
        // Set up GTR model: 4 gamma categories, gamma shape = 0.5
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        GeneralSubstitutionModel gsm = new GeneralSubstitutionModel();
        gsm.initByName("rates", "1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 4,
                "shape", "0.5",
                "substModel", gsm);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1949.0360143622, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", false, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1949.0360143622, BEASTTestCase.PRECISION);
    }

    @Test
    public void testGTRGILikelihood() throws Exception {
        // Set up GTR model: 4 gamma categories, gamma shape = 0.5, prop invariant = 0.5
        Alignment data = BEASTTestCase.getAlignment();
        Tree tree = BEASTTestCase.getTree(data);

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", data);

        GeneralSubstitutionModel gsm = new GeneralSubstitutionModel();
        gsm.initByName("rates", "1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0", "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 4,
                "shape", "0.5",
                "proportionInvariant", "0.5",
                "substModel", gsm);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1947.5829396144961, BEASTTestCase.PRECISION);

        likelihood.initByName("useAmbiguities", false, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        assertEquals(fLogP, -1947.5829396144961, BEASTTestCase.PRECISION);
    }

    void aminoacidModelTest(SubstitutionModel substModel, double fExpectedValue) throws Exception {
        Alignment data = BEASTTestCase.getAminoAcidAlignment();
        Tree tree = BEASTTestCase.getAminoAcidTree(data);
        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", substModel);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);
        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fExpectedValue, fLogP, BEASTTestCase.PRECISION);
    }

    @Test
    public void testAminoAcidLikelihoodWAG() throws Exception {
        // Set up WAG model
        WAG wag = new WAG();
        wag.initAndValidate();
        aminoacidModelTest(wag, -338.6388785157248);

    }

    @Test
    public void testAminoAcidLikelihoodJTT() throws Exception {
        // JTT
        JTT jtt = new JTT();
        jtt.initAndValidate();
        aminoacidModelTest(jtt, -338.80761792179726);

    }

    @Test
    public void testAminoAcidLikelihoodBlosum62() throws Exception {
        // Blosum62
        Blosum62 blosum62 = new Blosum62();
        blosum62.initAndValidate();
        aminoacidModelTest(blosum62, -345.3825963600176);

    }

    @Test
    public void testAminoAcidLikelihoodDayhoff() throws Exception {
        // Dayhoff
        Dayhoff dayhoff = new Dayhoff();
        dayhoff.initAndValidate();
        aminoacidModelTest(dayhoff, -340.6149187667345);
    }

    @Test
    public void testAminoAcidLikelihoodcpRev() throws Exception {
        // cpRev
        CPREV cpRev = new CPREV();
        cpRev.initAndValidate();
        aminoacidModelTest(cpRev, -348.71458467304154);
    }

    @Test
    public void testAminoAcidLikelihoodMTRev() throws Exception {
        // MTRev
        MTREV mtRev = new MTREV();
        mtRev.initAndValidate();
        aminoacidModelTest(mtRev, -369.4791633617842);

    }

    void aminoacidModelTestI(SubstitutionModel substModel, double fExpectedValue) throws Exception {
        Alignment data = BEASTTestCase.getAminoAcidAlignment();
        Tree tree = BEASTTestCase.getAminoAcidTree(data);
        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", substModel,
                "proportionInvariant", "0.2");

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);
        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fExpectedValue, fLogP, BEASTTestCase.PRECISION);
    }

    @Test
    public void testAminoAcidLikelihoodIWAG() throws Exception {
        // Set up WAG model
        WAG wag = new WAG();
        wag.initAndValidate();
        aminoacidModelTestI(wag, -338.7631166242887);
    }

    @Test
    public void testAminoAcidLikelihoodIJTT() throws Exception {
        // JTT
        JTT jtt = new JTT();
        jtt.initAndValidate();
        aminoacidModelTestI(jtt, -338.97566093453275);
    }

    @Test
    public void testAminoAcidLikelihoodIBlosum62() throws Exception {
        // Blosum62
        Blosum62 blosum62 = new Blosum62();
        blosum62.initAndValidate();
        aminoacidModelTestI(blosum62, -345.4456979614507);
    }

    @Test
    public void testAminoAcidLikelihoodIDayhoff() throws Exception {
        // Dayhoff
        Dayhoff dayhoff = new Dayhoff();
        dayhoff.initAndValidate();
        aminoacidModelTestI(dayhoff, -340.7630258641759);
    }

    @Test
    public void testAminoAcidLikelihoodIcpRev() throws Exception {
        // cpRev
        CPREV cpRev = new CPREV();
        cpRev.initAndValidate();
        aminoacidModelTestI(cpRev, -348.66316715026977);
    }

    @Test
    public void testAminoAcidLikelihoodIMTRev() throws Exception {
        // MTRev
        MTREV mtRev = new MTREV();
        mtRev.initAndValidate();
        aminoacidModelTestI(mtRev, -369.34449408200175);

    }

    void aminoacidModelTestIG(SubstitutionModel substModel, double fExpectedValue) throws Exception {
        Alignment data = BEASTTestCase.getAminoAcidAlignment();
        Tree tree = BEASTTestCase.getAminoAcidTree(data);
        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", substModel,
                "gammaCategoryCount", 4,
                "shape", "0.15",
                "proportionInvariant", "0.2");

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);
        double fLogP = 0;
        fLogP = likelihood.calculateLogP();
        assertEquals(fExpectedValue, fLogP, BEASTTestCase.PRECISION);
    }

    @Test
    public void testAminoAcidLikelihoodGIWAG() throws Exception {
        // Set up WAG model
        WAG wag = new WAG();
        wag.initAndValidate();
        aminoacidModelTestIG(wag, -342.69745607208495);
    }

    @Test
    public void testAminoAcidLikelihoodGIJTT() throws Exception {
        // JTT
        JTT jtt = new JTT();
        jtt.initAndValidate();
        aminoacidModelTestIG(jtt, -343.23738058653373);
    }

    @Test
    public void testAminoAcidLikelihoodGIBlosum62() throws Exception {
        // Blosum62
        Blosum62 blosum62 = new Blosum62();
        blosum62.initAndValidate();
        aminoacidModelTestIG(blosum62, -348.7305212479578);
    }

    @Test
    public void testAminoAcidLikelihoodGIDayhoff() throws Exception {
        // Dayhoff
        Dayhoff dayhoff = new Dayhoff();
        dayhoff.initAndValidate();
        aminoacidModelTestIG(dayhoff, -345.11861069556966);
    }

    @Test
    public void testAminoAcidLikelihoodGIcpRev() throws Exception {

        // cpRev
        CPREV cpRev = new CPREV();
        cpRev.initAndValidate();
        aminoacidModelTestIG(cpRev, -351.35553855806137);
    }

    @Test
    public void testAminoAcidLikelihoodGIMTRev() throws Exception {
        // MTRev
        MTREV mtRev = new MTREV();
        mtRev.initAndValidate();
        aminoacidModelTestIG(mtRev, -371.0038574147396);

    }

    @Test
    public void testSDolloLikelihood() throws Exception {
        UserDataType dataType = new UserDataType();
        dataType.initByName("states", 2, "codeMap", "0=1, 1=0, ?=0 1, -=0 1");
        Alignment data = new Alignment();

        Sequence German_ST = new Sequence("German_ST", BEASTTestCase.German_ST.dataInput.get());
        Sequence Dutch_List = new Sequence("Dutch_List", BEASTTestCase.Dutch_List.dataInput.get());
        ;
        Sequence English_ST = new Sequence("English_ST", BEASTTestCase.English_ST.dataInput.get());
        ;
        Sequence French = new Sequence("French", BEASTTestCase.French.dataInput.get());
        ;
        Sequence Italian = new Sequence("Italian", BEASTTestCase.Italian.dataInput.get());
        ;
        Sequence Spanish = new Sequence("Spanish", BEASTTestCase.Spanish.dataInput.get());
        ;


        data.initByName("sequence", German_ST, "sequence", Dutch_List, "sequence", English_ST, "sequence", French, "sequence", Italian, "sequence", Spanish,
                "userDataType", dataType
        );

        Tree tree = BEASTTestCase.getTree(data, "((English_ST:0.22743347188019544,(German_ST:0.10557648379843088,Dutch_List:0.10557648379843088):0.12185698808176457):1.5793160946109988,(Spanish:0.11078392189606047,(Italian:0.10119772534558173,French:0.10119772534558173):0.009586196550478737):1.6959656445951337)");

        RealParameter frequencies = new RealParameter("1 0");
        Frequencies freqs = new Frequencies();
        freqs.initByName("frequencies", frequencies);

        RealParameter deathprob = new RealParameter("1.7");
        MutationDeathModel SDollo = new MutationDeathModel();
        SDollo.initByName("deathprob", deathprob, "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", SDollo);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel, "useAmbiguities", true);
        fLogP = likelihood.calculateLogP();
        // beast1 xml gives -3551.6436
        assertEquals(fLogP, -3551.6436270344648, BEASTTestCase.PRECISION);
    }


    @Test
    public void testBinaryCovarionLikelihood() throws Exception {
        Alignment data = BEASTTestCase.getCovarionAlignment();
        Tree tree = BEASTTestCase.getTree(data, "((English_ST:0.22743347188019544,(German_ST:0.10557648379843088,Dutch_List:0.10557648379843088):0.12185698808176457):1.5793160946109988,(Spanish:0.11078392189606047,(Italian:0.10119772534558173,French:0.10119772534558173):0.009586196550478737):1.6959656445951337)");


        RealParameter alpha = new RealParameter("0.284");
        RealParameter switchRate = new RealParameter("0.829");
        RealParameter frequencies = new RealParameter("0.683 0.317");
        RealParameter hfrequencies = new RealParameter("0.5 0.5");
        BinaryCovarion covarion = new BinaryCovarion();
        covarion.initByName("alpha", alpha, "switchRate", switchRate, "vfrequencies", frequencies, "hfrequencies", hfrequencies);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName("mutationRate", "1.0", "gammaCategoryCount", 1, "substModel", covarion);

        TreeLikelihood likelihood = newTreeLikelihood();
        likelihood.initByName("data", data, "tree", tree, "siteModel", siteModel);

        double fLogP = 0;
        likelihood.initByName("useAmbiguities", true, "data", data, "tree", tree, "siteModel", siteModel);
        fLogP = likelihood.calculateLogP();
        // beast1 xml gives -1730.5363
        assertEquals(fLogP, -1730.53631739, BEASTTestCase.PRECISION);
    }

} // class TreeLikelihoodTest

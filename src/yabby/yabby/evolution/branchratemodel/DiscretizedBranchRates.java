package yabby.evolution.branchratemodel;

import yabby.core.Citation;
import yabby.core.Description;
import yabby.core.Input;
import yabby.core.parameter.IntegerParameter;
import yabby.evolution.tree.Node;
import yabby.evolution.tree.Tree.BaseTree;
import yabby.math.distributions.ParametricDistribution;

@Description("Defines an uncorrelated relaxed molecular clock.")
@Citation(value = "Drummond AJ, Ho SYW, Phillips MJ, Rambaut A (2006) Relaxed Phylogenetics and Dating with Confidence. PLoS Biol 4(5): e88", DOI = "10.1371/journal.pbio.0040088")
public class DiscretizedBranchRates extends BranchRateModel.Base {
    public Input<ParametricDistribution> rateDistInput = new Input<ParametricDistribution>("distr", "the distribution governing the rates among branches", Input.Validate.REQUIRED);
    public Input<IntegerParameter> categoryInput = new Input<IntegerParameter>("rateCategories", "the rate categories associated with nodes in the tree for sampling of individual rates among branches.", Input.Validate.REQUIRED);
    public Input<BaseTree> treeInput = new Input<BaseTree>("tree", "the tree this relaxed clock is associated with.", Input.Validate.REQUIRED);
    public Input<Boolean> normalizeInput = new Input<Boolean>("normalize", "Whether to normalize the average rate (default false).", false);
    public Input<Double> normalizeToInput = new Input<Double>("normalizeto", "the value to normalize, if normalising.", Double.NaN);
    
    private ParametricDistribution distributionModel;

    // The rate categories of each branch
    IntegerParameter rateCategories;

    private int categoryCount;
    private double step;
    private double[] rates;
    private boolean normalize = false;
    private double normalizeBranchRateTo = Double.NaN;
    private double scaleFactor = 1.0;
    private BaseTree tree;
    //private double logDensityNormalizationConstant;

	boolean recompute = true;

    //overSampling control the number of effective categories

    @Override
    public void initAndValidate() throws Exception {
    	int overSampling = 1;
//    public DiscretizedBranchRates(
//            TreeModel tree,
//            Parameter rateCategoryParameter,
//            ParametricDistributionModel model,
//            int overSampling,
//            boolean normalize,
//            double normalizeBranchRateTo) {

//        super(DiscretizedBranchRatesParser.DISCRETIZED_BRANCH_RATES);
    	tree = treeInput.get();
    	
    	rateCategories = categoryInput.get();
        rateCategories.setDimension(tree.getNodeCount() - 1);

        categoryCount = (tree.getNodeCount() - 1) * overSampling;

        step = 1.0 / (double) categoryCount;

        rates = new double[categoryCount];

        this.normalize = normalizeInput.get();

        this.distributionModel = rateDistInput.get();
        //this.normalizeBranchRateTo = normalizeBranchRateTo;

        Integer [] rates = new Integer[rateCategories.getDimension()];
        for (int i = 0; i < rateCategories.getDimension(); i++) {
            int index = (int) Math.floor((i + 0.5) * overSampling);
            rates[i] = index;
        }
        //Force the boundaries of rateCategoryParameter to match the category count
        IntegerParameter init = new IntegerParameter(rates);
        init.setBounds(0, categoryCount - 1);
        rateCategories.assignFromWithoutID(init);

        setupRates();

        // Each parameter take any value in [1, \ldots, categoryCount]
        // NB But this depends on the transition kernel employed.  Using swap-only results in a different constant
        //logDensityNormalizationConstant = -rateCategories.getDimension() * Math.log(categoryCount);
    }

    // compute scale factor

    private void computeFactor() {
        //scale mean rate to 1.0 or separate parameter
        double treeRate = 0.0;
        double treeTime = 0.0;

        //normalizeBranchRateTo = 1.0;
        for (int i = 0; i < tree.getNodeCount(); i++) {
            Node node = tree.getNode(i);
            if (!node.isRoot()) {
                int rateCategory = getRateCategory(tree, node);
                double branchLength = node.getParent().getHeight() - node.getHeight();
                treeRate += rates[rateCategory] * branchLength;
                treeTime += branchLength;

                //System.out.println("rates and time\t" + rates[rateCategory] + "\t" + treeModel.getBranchLength(node));
            }
        }
        //treeRate /= treeTime;

        scaleFactor = normalizeBranchRateTo / (treeRate / treeTime);
        //System.out.println("scaleFactor\t\t\t\t\t" + scaleFactor);
    }

    
    static int x = 0;
    public int getRateCategory(final BaseTree tree, final Node node) {
        // assert !tree.isRoot(node) : "root node doesn't have a rate!";
    	if (node.isRoot()) {
    		return 1;
    	}

        int nodeNumber = node.getNr();

        if (nodeNumber == rates.length) {
        	// root node has nr less than #categories, so use that nr
    		nodeNumber = node.getTree().getRoot().getNr();
        }
//System.err.println("x="+x++);
        int rateCategory = rateCategories.getValue(nodeNumber);
        return rateCategory;
    }
    
    /**
     * Calculates the actual rates corresponding to the category indices.
     */
    protected void setupRates() {

        double z = step / 2.0;

        try {
	        for (int i = 0; i < categoryCount; i++) {
	            //rates[i] = distributionModel.quantile(z);
	            rates[i] = distributionModel.inverseCumulativeProbability(z);
	            //System.out.print(rates[i]+"\t");
	        }
	    } catch (Exception e) {
	    	// Exception due to distribution not having  inverseCumulativeProbability implemented.
	    	// This should already been caught at initAndValidate()
	    	e.printStackTrace();
	    	System.exit(0);
	    }
        if (normalize) {
        	computeFactor();
        }
    }

	@Override
	public double getRateForBranch(Node node) {
		if (recompute) {
			setupRates();
			recompute = false;
		}
    	if (node.isRoot()) {
    		return 1;
    	}
        int rateCategory = getRateCategory(tree, node);
        return rates[rateCategory] * scaleFactor;
	}

    @Override
    protected boolean requiresRecalculation() {
    	recompute = false;

        if (treeInput.get().somethingIsDirty()) {
        	recompute = true;
            return true;
        }
        // rateDistInput cannot be dirty?!?
        if (rateDistInput.get().isDirtyCalculation()) {
        	recompute = true;
        	return true;
        }
        // NOT processed as trait on the tree, so DO mark as dirty
        if (categoryInput.get().somethingIsDirty()) {
        	recompute = true;
        	return true;
        }
        return recompute;
    }

    @Override
    public void store() {
    	super.store();
    }
    @Override
    public void restore() {
    	super.restore();
    	recompute = true;
    }
}

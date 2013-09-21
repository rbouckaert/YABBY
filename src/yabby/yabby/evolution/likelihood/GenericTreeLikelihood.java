package yabby.evolution.likelihood;

import java.util.List;
import java.util.Random;

import yabby.core.Description;
import yabby.core.Distribution;
import yabby.core.Input;
import yabby.core.State;
import yabby.core.Input.Validate;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.branchratemodel.BranchRateModel;
import yabby.evolution.sitemodel.SiteModelInterface;
import yabby.evolution.tree.Tree;
import yabby.evolution.tree.TreeInterface;




@Description("Generic tree likelihood for an alignment given a generic SiteModel, " +
		"a beast tree and a branch rate model")
// Use this as base class to define any non-standard TreeLikelihood.
// Override Distribution.calculatLogP() to make this class functional.
//
// TODO: This could contain a generic traverse() method that takes dirty trees in account.
//
public class GenericTreeLikelihood extends Distribution {
    
    public Input<Alignment> dataInput = new Input<Alignment>("data", "sequence data for the beast.tree", Validate.REQUIRED);

    public Input<TreeInterface> treeInput = new Input<TreeInterface>("tree", "phylogenetic beast.tree with sequence data in the leafs", Validate.REQUIRED);

    public Input<SiteModelInterface> siteModelInput = new Input<SiteModelInterface>("siteModel", "site model for leafs in the beast.tree", Validate.REQUIRED);
    
    public Input<BranchRateModel.Base> branchRateModelInput = new Input<BranchRateModel.Base>("branchRateModel",
            "A model describing the rates on the branches of the beast.tree.");

    
    
	@Override
	public List<String> getArguments() {return null;}

	@Override
	public List<String> getConditions() {return null;}

	@Override
	public void sample(State state, Random random) {}

}

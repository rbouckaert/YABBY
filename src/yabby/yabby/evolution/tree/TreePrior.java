package yabby.evolution.tree;


import java.util.List;
import java.util.Random;

import yabby.core.Description;
import yabby.core.Distribution;
import yabby.core.Input;
import yabby.core.State;
import yabby.core.Input.Validate;
import yabby.evolution.tree.coalescent.TreeIntervals;



@Description("Prior on a tree, such as Coalescent or Yule")
public class TreePrior extends Distribution {
	public Input<Tree> treeInput = new Input<Tree>("tree", "species tree over which to calculate speciation likelihood");
    public Input<TreeIntervals> treeIntervals = new Input<TreeIntervals>("treeIntervals", "Intervals for a phylogenetic beast tree", Validate.XOR, treeInput);

	@Override
	public List<String> getArguments() {
		return null;
	}

	@Override
	public List<String> getConditions() {
		return null;
	}

	@Override
	public void sample(State state, Random random) {
	}

    @Override
    protected boolean requiresRecalculation() {
        final TreeIntervals ti = treeIntervals.get();
        return (ti != null && ti.isDirtyCalculation()) || treeInput.get().somethingIsDirty();
    }
}

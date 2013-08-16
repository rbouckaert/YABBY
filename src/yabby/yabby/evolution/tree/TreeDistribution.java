package yabby.evolution.tree;


import java.util.List;
import java.util.Random;

import yabby.core.Description;
import yabby.core.Distribution;
import yabby.core.Input;
import yabby.core.State;
import yabby.core.Input.Validate;
import yabby.evolution.tree.coalescent.TreeIntervals;

@Description("Distribution on a tree, typically a prior such as Coalescent or Yule")
public class TreeDistribution extends Distribution {
    public Input<Tree> m_tree = new Input<Tree>("tree", "tree over which to calculate a prior or likelihood");
    public Input<TreeIntervals> treeIntervals = new Input<TreeIntervals>("treeIntervals", "Intervals for a phylogenetic beast tree", Validate.XOR, m_tree);

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
        if (ti != null) {
            //boolean d = ti.isDirtyCalculation();
            //assert d;
            assert ti.isDirtyCalculation();
            return true;
        }
        return m_tree.get().somethingIsDirty();
    }
    
 	/** Indicate that the tree distribution can deal with dated tips in the tree
	 * Some tree distributions like the Yule prior cannot handle this.
	 * @return true by default
	 */
	public boolean canHandleTipDates() {
		return true;
	}
}

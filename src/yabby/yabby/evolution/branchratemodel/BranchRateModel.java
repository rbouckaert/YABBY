package yabby.evolution.branchratemodel;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Input;
import yabby.core.parameter.RealParameter;
import yabby.evolution.tree.Node;

/**
 * @author Alexei Drummond
 */
@Description("Defines a mean rate for each branch in the beast.tree.")
public interface BranchRateModel {

    public double getRateForBranch(Node node);

    @Description(value = "Base implementation of a clock model.", isInheritable = false)
    public abstract class Base extends CalculationNode implements BranchRateModel {
        public Input<RealParameter> meanRateInput = new Input<RealParameter>("clock.rate", "mean clock rate (defaults to 1.0)");

        // empty at the moment but brings together the required interfaces
    }
}

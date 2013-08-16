package yabby.evolution.tree.coalescent;


import java.util.List;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.core.parameter.RealParameter;
import yabby.evolution.tree.coalescent.PopulationFunction;

/**
 * @author Joseph Heled
 *         Date: 2/03/2011
 */

@Description("Scale a demographic function by a constant factor")
public class ScaledPopulationFunction extends PopulationFunction.Abstract {
    public Input<PopulationFunction> popParameter = new Input<PopulationFunction>("population",
            "population function to scale. ", Validate.REQUIRED);

    public Input<RealParameter> scaleFactor = new Input<RealParameter>("factor",
            "scale population by this facor.", Validate.REQUIRED);

    public ScaledPopulationFunction() {
    }

    // Implementation of abstract methods

    public List<String> getParameterIds() {
        List<String> ids = popParameter.get().getParameterIds();
        ids.add(scaleFactor.get().getID());
        return ids;
    }

    public double getPopSize(double t) {
        return popParameter.get().getPopSize(t) * scaleFactor.get().getValue();
    }

    public double getIntensity(double t) {
        double fIntensity = popParameter.get().getIntensity(t);
        double fScale = scaleFactor.get().getValue();
        return fIntensity / fScale;
    }

    public double getInverseIntensity(double x) {
        throw new RuntimeException("unimplemented");
    }

    @Override
    protected boolean requiresRecalculation() {
        return ((CalculationNode) popParameter.get()).isDirtyCalculation() || scaleFactor.get().somethingIsDirty();
    }
}

package yabby.core.parameter;

import java.util.ArrayList;
import java.util.List;

import yabby.core.YABBYObject;
import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Function;
import yabby.core.Input;
import yabby.core.StateNode;
import yabby.core.Input.Validate;




@Description("Summarizes a set of valuables so that for example a rate matrix can be " +
        "specified that uses a parameter in various places in the matrix.")
public class CompoundValuable extends CalculationNode implements Function {
    public Input<List<YABBYObject>> m_values = new Input<List<YABBYObject>>("var", "reference to a valuable",
            new ArrayList<YABBYObject>(), Validate.REQUIRED, Function.class);

    boolean m_bRecompute = true;
    /**
     * contains values of the inputs *
     */
    double[] m_fValues;

    @Override
    public void initAndValidate() throws Exception {
        // determine dimension
        int nDimension = 0;
        for (YABBYObject plugin : m_values.get()) {
            if (!(plugin instanceof Function)) {
                throw new Exception("Input does not implement Valuable");
            }
            nDimension += ((Function) plugin).getDimension();
        }
        m_fValues = new double[nDimension];
    }

    /**
     * Valuable implementation follows *
     */
    @Override
    public int getDimension() {
        return m_fValues.length;
    }

    @Override
    public double getArrayValue() {
        if (m_bRecompute) {
            recompute();
        }
        return m_fValues[0];
    }

    @Override
    public double getArrayValue(int iDim) {
        if (m_bRecompute) {
            recompute();
        }
        return m_fValues[iDim];
    }

    /**
     * collect values of the compounds into an array *
     */
    private void recompute() {
        int k = 0;
        for (YABBYObject plugin : m_values.get()) {
            Function valuable = (Function) plugin;
            if (plugin instanceof StateNode) {
                valuable = ((StateNode) plugin).getCurrent();
            }
            int nDimension = valuable.getDimension();
            for (int i = 0; i < nDimension; i++) {
                m_fValues[k++] = valuable.getArrayValue(i);
            }
        }
        m_bRecompute = false;
    }

    /**
     * CalculationNode methods *
     */
    @Override
    public void store() {
        m_bRecompute = true;
        super.store();
    }

    @Override
    public void restore() {
        m_bRecompute = true;
        super.restore();
    }

    @Override
    public boolean requiresRecalculation() {
        m_bRecompute = true;
        return true;
    }

}

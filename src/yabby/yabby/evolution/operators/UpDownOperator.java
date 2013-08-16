package yabby.evolution.operators;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Operator;
import yabby.core.StateNode;
import yabby.core.Input.Validate;
import yabby.core.parameter.Parameter;
import yabby.core.parameter.RealParameter;
import yabby.util.Randomizer;

@Description("This element represents an operator that scales two parameters in different directions. " +
        "Each operation involves selecting a scale uniformly at random between scaleFactor and 1/scaleFactor. " +
        "The up parameter is multiplied by this scale and the down parameter is divided by this scale.")
public class UpDownOperator extends Operator {

    public Input<Double> m_scaleFactor = new Input<Double>("scaleFactor",
            "magnitude factor used for scaling", Validate.REQUIRED);
    public Input<List<StateNode>> m_up = new Input<List<StateNode>>("up",
            "zero or more items to scale upwards", new ArrayList<StateNode>());
    public Input<List<StateNode>> m_down = new Input<List<StateNode>>("down",
            "zero or more items to scale downwards", new ArrayList<StateNode>());
    public Input<Boolean> m_bOptimise = new Input<Boolean>("optimise", "flag to indicate that the scale factor is automatically changed in order to acheive a good acceptance rate (default true)", true);
    public Input<Boolean> elementWise = new Input<Boolean>("elementWise", "flag to indicate that the scaling is applied to a random index in multivariate parameters (default false)", false);

    double m_fScaleFactor;

    @Override
    public void initAndValidate() throws Exception {
        m_fScaleFactor = m_scaleFactor.get();
        // sanity checks
        if (m_up.get().size() + m_down.get().size() == 0) {
            System.err.println("WARNING: At least one up or down item must be specified");
        }
        if (m_up.get().size() == 0 || m_down.get().size() == 0) {
            System.err.println("WARNING: no " + (m_up.get().size() == 0 ? "up" : "down") + " item specified in UpDownOperator");
        }
    }

    /**
     * override this for proposals,
     *
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal
     *         should not be accepted
     */
    @Override
    public final double proposal() {

        final double scale = (m_fScaleFactor + (Randomizer.nextDouble() * ((1.0 / m_fScaleFactor) - m_fScaleFactor)));
        int goingUp = 0, goingDown = 0;


        if (elementWise.get()) {
            int size = 0;
            for (StateNode up : m_up.get()) {
                if (size == 0) size = up.getDimension();
                if (size > 0 && up.getDimension() != size) {
                    throw new RuntimeException("elementWise=true but parameters of differing lengths!");
                }
                goingUp += 1;
            }

            for (StateNode down : m_down.get()) {
                if (size == 0) size = down.getDimension();
                if (size > 0 && down.getDimension() != size) {
                    throw new RuntimeException("elementWise=true but parameters of differing lengths!");
                }
                goingDown += 1;
            }

            int index = Randomizer.nextInt(size);

            for (StateNode up : m_up.get()) {
                if (up instanceof RealParameter) {
                    RealParameter p = (RealParameter) up;
                    p.setValue(p.getValue(index) * scale);
                }
                if (outsideBounds(up)) {
                    return Double.NEGATIVE_INFINITY;
                }
            }

            for (StateNode down : m_down.get()) {
                if (down instanceof RealParameter) {
                    RealParameter p = (RealParameter) down;
                    p.setValue(p.getValue(index) / scale);
                }
                if (outsideBounds(down)) {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        } else {

            try {
                for (StateNode up : m_up.get()) {
                    up = up.getCurrentEditable(this);
                    goingUp += up.scale(scale);
                    if (outsideBounds(up)) {
                        return Double.NEGATIVE_INFINITY;
                    }
                }

                for (StateNode down : m_down.get()) {
                    down = down.getCurrentEditable(this);
                    goingDown += down.scale(1.0 / scale);
                    if (outsideBounds(down)) {
                        return Double.NEGATIVE_INFINITY;
                    }
                }
            } catch (Exception e) {
                // scale resulted in invalid StateNode, abort proposal
                return Double.NEGATIVE_INFINITY;
            }
        }
        return (goingUp - goingDown - 2) * Math.log(scale);
    }

    private boolean outsideBounds(final StateNode node) {
        if (node instanceof Parameter<?>) {
            final Parameter<?> p = (Parameter) node;
            final Double lower = (Double) p.getLower();
            final Double upper = (Double) p.getUpper();
            final Double value = (Double) p.getValue();
            if (value < lower || value > upper) {
                return true;
            }
        }
        return false;
    }

    /**
     * automatic parameter tuning *
     */
    @Override
    public void optimize(final double logAlpha) {
        if (m_bOptimise.get()) {
            double fDelta = calcDelta(logAlpha);
            fDelta += Math.log(1.0 / m_fScaleFactor - 1.0);
            m_fScaleFactor = 1.0 / (Math.exp(fDelta) + 1.0);
        }
    }

    @Override
    public double getCoercableParameterValue() {
        return m_fScaleFactor;
    }

    @Override
    public void setCoercableParameterValue(final double fValue) {
        m_fScaleFactor = fValue;
    }

    @Override
    public String getPerformanceSuggestion() {
        final double prob = m_nNrAccepted / (m_nNrAccepted + m_nNrRejected + 0.0);
        final double targetProb = getTargetAcceptanceProbability();

        double ratio = prob / targetProb;
        if (ratio > 2.0) ratio = 2.0;
        if (ratio < 0.5) ratio = 0.5;

        // new scale factor
        final double sf = Math.pow(m_fScaleFactor, ratio);

        final DecimalFormat formatter = new DecimalFormat("#.###");
        if (prob < 0.10) {
            return "Try setting scaleFactor to about " + formatter.format(sf);
        } else if (prob > 0.40) {
            return "Try setting scaleFactor to about " + formatter.format(sf);
        } else return "";
    }
} // class UpDownOperator

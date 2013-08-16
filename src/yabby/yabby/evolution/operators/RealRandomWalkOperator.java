package yabby.evolution.operators;

import java.text.DecimalFormat;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Operator;
import yabby.core.Input.Validate;
import yabby.core.parameter.RealParameter;
import yabby.util.Randomizer;



@Description("A random walk operator that selects a random dimension of the real parameter and perturbs the value a " +
        "random amount within +/- windowSize.")
public class RealRandomWalkOperator extends Operator {
    public Input<Double> windowSizeInput =
            new Input<Double>("windowSize", "the size of the window both up and down when using uniform interval OR standard deviation when using Gaussian", Input.Validate.REQUIRED);
    public Input<RealParameter> parameterInput =
            new Input<RealParameter>("parameter", "the parameter to operate a random walk on.", Validate.REQUIRED);
    public Input<Boolean> useGaussianInput =
            new Input<Boolean>("useGaussian", "Use Gaussian to move instead of uniform interval. Default false.", false);

    double windowSize = 1;
    boolean m_bUseGaussian;

    public void initAndValidate() {
        windowSize = windowSizeInput.get();
        m_bUseGaussian = useGaussianInput.get();
    }

    /**
     * override this for proposals,
     * returns log of hastingRatio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {

        RealParameter param = parameterInput.get(this);

        int i = Randomizer.nextInt(param.getDimension());
        double value = param.getValue(i);
        double newValue = value;
        if (m_bUseGaussian) {
            newValue += Randomizer.nextGaussian() * windowSize;
        } else {
            newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
        }

        if (newValue < param.getLower() || newValue > param.getUpper()) {
            return Double.NEGATIVE_INFINITY;
        }
        if (newValue == value) {
            // this saves calculating the posterior
            return Double.NEGATIVE_INFINITY;
        }

        param.setValue(i, newValue);

        return 0.0;
    }


    @Override
    public double getCoercableParameterValue() {
        return windowSize;
    }

    @Override
    public void setCoercableParameterValue(double fValue) {
        windowSize = fValue;
    }

    /**
     * called after every invocation of this operator to see whether
     * a parameter can be optimised for better acceptance hence faster
     * mixing
     *
     * @param logAlpha difference in posterior between previous state & proposed state + hasting ratio
     */
    @Override
    public void optimize(double logAlpha) {
        // must be overridden by operator implementation to have an effect
        double fDelta = calcDelta(logAlpha);

        fDelta += Math.log(windowSize);
        windowSize = Math.exp(fDelta);
    }

    @Override
    public final String getPerformanceSuggestion() {
        double prob = m_nNrAccepted / (m_nNrAccepted + m_nNrRejected + 0.0);
        double targetProb = getTargetAcceptanceProbability();

        double ratio = prob / targetProb;
        if (ratio > 2.0) ratio = 2.0;
        if (ratio < 0.5) ratio = 0.5;

        // new scale factor
        double newWindowSize = windowSize * ratio;

        DecimalFormat formatter = new DecimalFormat("#.###");
        if (prob < 0.10) {
            return "Try setting window size to about " + formatter.format(newWindowSize);
        } else if (prob > 0.40) {
            return "Try setting window size to about " + formatter.format(newWindowSize);
        } else return "";
    }
} // class RealRandomWalkOperator
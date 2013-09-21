package yabby.math.distributions;


import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.parameter.RealParameter;




@Description("Normal distribution.  f(x) = frac{1}{\\sqrt{2\\pi\\sigma^2}} e^{ -\\frac{(x-\\mu)^2}{2\\sigma^2} } " +
        "If the input x is a multidimensional parameter, each of the dimensions is considered as a " +
        "separate independent component.")
public class Normal extends ParametricDistribution {
    public Input<RealParameter> meanInput = new Input<RealParameter>("mean", "mean of the normal distribution, defaults to 0");
    public Input<RealParameter> sigmaInput = new Input<RealParameter>("sigma", "variance of the normal distribution, defaults to 1");

    static org.apache.commons.math.distribution.NormalDistribution dist = new NormalDistributionImpl(0, 1);

    @Override
    public void initAndValidate() {
        refresh();
    }

    /**
     * make sure internal state is up to date *
     */
    void refresh() {
        double fMean;
        double fSigma;
        if (meanInput.get() == null) {
            fMean = 0;
        } else {
            fMean = meanInput.get().getValue();
        }
        if (sigmaInput.get() == null) {
            fSigma = 1;
        } else {
            fSigma = sigmaInput.get().getValue();
        }
        dist.setMean(fMean);
        dist.setStandardDeviation(fSigma);
    }

    @Override
    public ContinuousDistribution getDistribution() {
        refresh();
        return dist;
    }

    @Override
    public double getMean() {
        if (meanInput.get() == null) {
        	return offsetInput.get();
        } else {
        	return offsetInput.get() + meanInput.get().getValue();
        }
    }
} // class Normal

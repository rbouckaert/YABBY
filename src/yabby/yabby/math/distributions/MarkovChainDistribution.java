package yabby.math.distributions;


import java.util.List;
import java.util.Random;

import org.apache.commons.math.distribution.GammaDistribution;
import org.apache.commons.math.distribution.GammaDistributionImpl;

import yabby.core.Description;
import yabby.core.Distribution;
import yabby.core.Input;
import yabby.core.State;
import yabby.core.Input.Validate;
import yabby.core.parameter.RealParameter;
import yabby.math.distributions.LogNormalDistributionModel.LogNormalImpl;




/**
 * Initial version Ported from Beast 1.7 ExponentialMarkovModel
 */
@Description("A class that produces a distribution chaining values in a parameter through the Gamma distribution. " +
        "The value of a parameter is assumed to be Gamma distributed with mean as the previous value in the parameter. " +
		"If useLogNormal is set, a log normal distribution is used instead of a Gamma. " +
        "If a Jeffrey's prior is used, the first value is assumed to be distributed as 1/x, otherwise it is assumed to be uniform. " +
        "Handy for population parameters. ")
public class MarkovChainDistribution extends Distribution {

    public Input<Boolean> isJeffreysInput = new Input<Boolean>("jeffreys", "use Jeffrey's prior (default false)", false);
    public Input<Boolean> isReverseInput = new Input<Boolean>("reverse", "parameter in reverse (default false)", false);
    public Input<Boolean> useLogInput = new Input<Boolean>("uselog", "use logarithm of parameter values (default false)", false);
    public Input<Double> shapeInput = new Input<Double>("shape", "shape parameter of the Gamma distribution (default 1.0 = exponential distribution) " +
    		" or precision parameter if the log normal is used.", 1.0);
    public Input<RealParameter> parameterInput = new Input<RealParameter>("parameter", "chain parameter to calculate distribution over", Validate.REQUIRED);
    public Input<Boolean> useLogNormalInput = new Input<Boolean>("useLogNormal", "use Log Normal distribution instead of Gamma (default false)", false);

    // **************************************************************
    // Private instance variables
    // **************************************************************
    private RealParameter chainParameter = null;
    private boolean jeffreys = false;
    private boolean reverse = false;
    private boolean uselog = false;
    private double shape = 1.0;
    GammaDistribution gamma;
    LogNormalImpl logNormal;
    boolean useLogNormal;

    @Override
    public void initAndValidate() throws Exception {
        reverse = isReverseInput.get();
        jeffreys = isJeffreysInput.get();
        uselog = useLogInput.get();
        shape = shapeInput.get();
        chainParameter = parameterInput.get();
        useLogNormal = useLogNormalInput.get();
        gamma = new GammaDistributionImpl(shape, 1);
        logNormal = new LogNormalDistributionModel().new LogNormalImpl(1, 1);
    }


    /**
     * Get the log likelihood.
     *
     * @return the log likelihood.
     */
    @Override
    public double calculateLogP() throws Exception {
        logP = 0.0;
        // jeffreys Prior!
        if (jeffreys) {
            logP += -Math.log(getChainValue(0));
        }
        for (int i = 1; i < chainParameter.getDimension(); i++) {
            final double mean = getChainValue(i - 1);
            final double x = getChainValue(i);

            if (useLogNormal) {
	            final double sigma = 1.0 / shape; // shape = precision
	            // convert mean to log space
	            final double M = Math.log(mean) - (0.5 * sigma * sigma);
	            logNormal.setMeanAndStdDev(M, sigma);
	            logP += logNormal.logDensity(x);
            } else {
                final double scale = mean / shape;
                gamma.setBeta(scale);
                logP += gamma.logDensity(x);
            }
        }
        return logP;
    }

    private double getChainValue(int i) {
        if (uselog) {
            return Math.log(chainParameter.getValue(index(i)));
        } else {
            return chainParameter.getValue(index(i));
        }
    }

    private int index(int i) {
        if (reverse)
            return chainParameter.getDimension() - i - 1;
        else
            return i;
    }

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
}


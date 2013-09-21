
package yabby.evolution.tree.coalescent;

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




/** Ported from Beast 1.7 */
@Description("A class that produces a distribution chaining values in a parameter through the Gamma distribution. " +
		"The value of a parameter is assumed to be Gamma distributed with mean as the previous value in the parameter. " +
		"If a Jeffrey's prior is used, the first value is assumed to be distributed as 1/x, otherwise it is assumed to be uniform. " +
		"Handy for population parameters. ")
public class ExponentialMarkovLikelihood extends Distribution {

	public Input<Boolean> isJeffreysInput = new Input<Boolean>("jeffreys", "use Jeffrey's prior (default false)", false);
	public Input<Boolean> isReverseInput = new Input<Boolean>("reverse", "parameter in reverse (default false)", false);
	public Input<Double> shapeInput = new Input<Double>("shape", "shape parameter of the Gamma distribution (default 1.0 = exponential distribution)", 1.0);
	public Input<RealParameter> parameterInput = new Input<RealParameter>("parameter", "chain parameter to calculate distribution over", Validate.REQUIRED);
	
    // **************************************************************
    // Private instance variables
    // **************************************************************
    private RealParameter chainParameter = null;
    private boolean jeffreys = false;
    private boolean reverse = false;
    private double shape = 1.0;
    GammaDistribution gamma;

	@Override
	public void initAndValidate() throws Exception {
		reverse = isReverseInput.get();
		jeffreys = isJeffreysInput.get();
		shape = shapeInput.get();
		chainParameter = parameterInput.get();
		gamma = new GammaDistributionImpl(shape, 1);
	}


    private int index(int i) {
        if (reverse)
            return chainParameter.getDimension() - i - 1;
        else
            return i;
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
            logP += -Math.log(chainParameter.getValue(index(0)));
        }
        for (int i = 1; i < chainParameter.getDimension(); i++) {
            final double mean = chainParameter.getValue(index(i - 1));
            final double x = chainParameter.getValue(index(i));
            //logL += dr.math.distributions.ExponentialDistribution.logPdf(x, 1.0/mean);

            final double scale = mean / shape;
            gamma.setBeta(scale);
            logP += gamma.logDensity(x);//logPdf(x, shape, scale);
        }
        return logP;
    }


	@Override
	public List<String> getArguments() {return null;}
	@Override
	public List<String> getConditions() {return null;}
	@Override
	public void sample(State state, Random random) {}
}


package yabby.math.distributions;


import org.apache.commons.math.distribution.PoissonDistributionImpl;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.parameter.RealParameter;




@Description("Poisson distribution, used as prior  f(k; lambda)=\\frac{lambda^k e^{-lambda}}{k!}  " +
        "If the input x is a multidimensional parameter, each of the dimensions is considered as a " +
        "separate independent component.")
public class Poisson extends ParametricDistribution {
    public Input<RealParameter> lambdaInput = new Input<RealParameter>("lambda", "rate parameter, defaults to 1");

    static org.apache.commons.math.distribution.PoissonDistribution dist = new PoissonDistributionImpl(1);

    @Override
    public void initAndValidate() {
        refresh();
    }

    /**
     * make sure internal state is up to date *
     */
    void refresh() {
        double m_fLambda;
        if (lambdaInput.get() == null) {
            m_fLambda = 1;
        } else {
            m_fLambda = lambdaInput.get().getValue();
            if (m_fLambda < 0) {
                m_fLambda = 1;
            }
        }
        dist.setMean(m_fLambda);
    }

    @Override
    public org.apache.commons.math.distribution.Distribution getDistribution() {
        refresh();
        return dist;
    }

} // class Poisson

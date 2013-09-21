package yabby.math.distributions;


import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.GammaDistributionImpl;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.parameter.RealParameter;




@Description("Gamma distribution.    for x>0  g(x;alpha,beta) = \\frac{beta^{alpha}}{Gamma(alpha)} x^{alpha-1} e^{-beta {x}}" +
        "If the input x is a multidimensional parameter, each of the dimensions is considered as a " +
        "separate independent component.")
public class Gamma extends ParametricDistribution {
    public Input<RealParameter> alphaInput = new Input<RealParameter>("alpha", "shape parameter, defaults to 2");
    public Input<RealParameter> betaInput = new Input<RealParameter>("beta", "scale parameter, defaults to 2");

    static org.apache.commons.math.distribution.GammaDistribution m_dist = new GammaDistributionImpl(1, 1);

    @Override
    public void initAndValidate() {
        refresh();
    }

    /**
     * make sure internal state is up to date *
     */
    void refresh() {
        double fAlpha;
        double fBeta;
        if (alphaInput.get() == null) {
            fAlpha = 2;
        } else {
            fAlpha = alphaInput.get().getValue();
        }
        if (betaInput.get() == null) {
            fBeta = 2;
        } else {
            fBeta = betaInput.get().getValue();
        }
        m_dist.setAlpha(fAlpha);
        m_dist.setBeta(fBeta);
    }

    @Override
    public ContinuousDistribution getDistribution() {
        refresh();
        return m_dist;
    }

    @Override
    public double getMean() {
    	return offsetInput.get() + m_dist.getAlpha() / m_dist.getBeta();
    }
} // class Gamma

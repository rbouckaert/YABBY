package yabby.evolution.operators;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.evolution.alignment.TaxonSet;
import yabby.evolution.tree.Node;
import yabby.util.Randomizer;


@Description("Randomly moves tip dates on a tree by randomly selecting one from (a subset of) taxa")
public class TipDatesRandomWalker extends TreeOperator {
    // perhaps multiple trees may be necessary if they share the same taxon?
    // public Input<List<Tree>> m_treesInput = new Input<List<Tree>>("tree" ,"tree to operate on", new ArrayList<Tree>(), Validate.REQUIRED);

    public Input<Double> windowSizeInput =
            new Input<Double>("windowSize", "the size of the window both up and down when using uniform interval OR standard deviation when using Gaussian", Input.Validate.REQUIRED);
    public Input<TaxonSet> m_taxonsetInput = new Input<TaxonSet>("taxonset", "limit scaling to a subset of taxa. By default all tips are scaled.");
    public Input<Boolean> useGaussianInput =
            new Input<Boolean>("useGaussian", "Use Gaussian to move instead of uniform interval. Default false.", false);

    /**
     * node indices of taxa to choose from *
     */
    int[] m_iTaxa;

    double windowSize = 1;
    boolean m_bUseGaussian;

    /**
     * whether to reflect random values from boundaries or absorb *
     */
    boolean reflectValue = true;

    @Override
    public void initAndValidate() throws Exception {
        windowSize = windowSizeInput.get();
        m_bUseGaussian = useGaussianInput.get();

        // determine taxon set to choose from
        if (m_taxonsetInput.get() != null) {
            List<String> sTaxaNames = new ArrayList<String>();
            for (String sTaxon : treeInput.get().getTaxaNames()) {
                sTaxaNames.add(sTaxon);
            }

            List<String> set = m_taxonsetInput.get().asStringList();
            int nNrOfTaxa = set.size();
            m_iTaxa = new int[nNrOfTaxa];
            int k = 0;
            for (String sTaxon : set) {
                int iTaxon = sTaxaNames.indexOf(sTaxon);
                if (iTaxon < 0) {
                    throw new Exception("Cannot find taxon " + sTaxon + " in tree");
                }
                m_iTaxa[k++] = iTaxon;
            }
        } else {
            m_iTaxa = new int[treeInput.get().getTaxaNames().length];
            for (int i = 0; i < m_iTaxa.length; i++) {
                m_iTaxa[i] = i;
            }
        }
    }

    @Override
    public double proposal() {
        // randomly select leaf node
        int i = Randomizer.nextInt(m_iTaxa.length);
        Node node = treeInput.get().getNode(m_iTaxa[i]);

        double value = node.getHeight();
        double newValue = value;
        if (m_bUseGaussian) {
            newValue += Randomizer.nextGaussian() * windowSize;
        } else {
            newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
        }


        if (newValue > node.getParent().getHeight()) { // || newValue < 0.0) {
            if (reflectValue) {
                newValue = reflectValue(newValue, 0.0, node.getParent().getHeight());
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }
        if (newValue == value) {
            // this saves calculating the posterior
            return Double.NEGATIVE_INFINITY;
        }
        node.setHeight(newValue);

        return 0.0;
    }


    public double reflectValue(double value, double lower, double upper) {

        double newValue = value;

        if (value < lower) {
            if (Double.isInfinite(upper)) {
                // we are only going to reflect once as the upper bound is at infinity...
                newValue = lower + (lower - value);
            } else {
                double remainder = lower - value;

                int widths = (int) Math.floor(remainder / (upper - lower));
                remainder -= (upper - lower) * widths;

                // even reflections
                if (widths % 2 == 0) {
                    newValue = lower + remainder;
                    // odd reflections
                } else {
                    newValue = upper - remainder;
                }
            }
        } else if (value > upper) {
            if (Double.isInfinite(lower)) {
                // we are only going to reflect once as the lower bound is at -infinity...
                newValue = upper - (newValue - upper);
            } else {

                double remainder = value - upper;

                int widths = (int) Math.floor(remainder / (upper - lower));
                remainder -= (upper - lower) * widths;

                // even reflections
                if (widths % 2 == 0) {
                    newValue = upper - remainder;
                    // odd reflections
                } else {
                    newValue = lower + remainder;
                }
            }
        }

        return newValue;
    }


    @Override
    public double getCoercableParameterValue() {
        return windowSize;
    }

    @Override
    public void setCoercableParameterValue(double fValue) {
        windowSize = fValue;
    }

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
}

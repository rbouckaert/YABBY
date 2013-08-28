package yabby.core.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.YABBYObject;
import yabby.core.Function;
import yabby.core.Input.Validate;

//import beast.core.Distribution;

@Description("Report effective sample size of a parameter or log values from a distribution. " +
        "This uses the same criterion as Tracer and assumes 10% burn in.")
public class ESS extends YABBYObject implements Loggable {
    public Input<Function> functionInput =
            new Input<Function>("arg", "value (e.g. parameter or distribution) to report ESS for", Validate.REQUIRED);

    /**
     * values from which the ESS is calculated *
     */
    List<Double> trace;
    /**
     * sum of trace, excluding burn-in *
     */
    double sum = 0;
    /**
     * keep track of sums of trace(i)*trace(i_+ lag) for all lags, excluding burn-in  *
     */
    List<Double> squareLaggedSums;

    @Override
    public void initAndValidate() {
        trace = new ArrayList<Double>();
        squareLaggedSums = new ArrayList<Double>();
    }

    @Override
    public void init(PrintStream out) throws Exception {
        final String sID = ((YABBYObject) functionInput.get()).getID();
        out.print("ESS(" + sID + ")\t");
    }

    final static int MAX_LAG = 2000;

//  We determine the Effective Sample Size (ESS) based on the auto correlation (AC) between the sequence and the same
//  sequence delayed by some amount.  For a highly correlated sequence the AC will be high for a small delay,
//  and is expected to drop to around zero when the delay is large enough. The delay when the AC is zero is the ACT (auto
//  correlation time), and the ESS is the number of samples remaining when keeping only one sample out of every ACT.
//
//  The (squared) auto correlation between two sequences is the covariance divided by the product of the individual
//  variances. Since both sequences are essentially the same sequence we do not bother to scale.
//
//  The simplest criteria to use to find the point where the AC "gets" to zero is to take the first time it becomes
//  negative. This is deemed too simple and instead we first find the approximate point - the first time where the sum of
//  two consecutive values is negative, and then determine the ACT by assuming the AC - as a function of the delay - is
//  roughly linear and so the ACT (the point on the X axis) is approximately equal to twice the area under the curve divided
//  by the value at x=0 (the AC of the sequence). This is the reason for summing up twice the variances inside the loop - a
//  basic numerical integration technique.

    @Override
    public void log(final int nSample, PrintStream out) {
//		final Double fNewValue = (m_distribution == null? m_pParam.get().getValue() : m_distribution.getCurrentLogP());
        final Double fNewValue = functionInput.get().getArrayValue();
        trace.add(fNewValue);
        sum += fNewValue;

        final int nTotalSamples = trace.size();

        // take 10% burn in
        final int iStart = nTotalSamples / 10;
        if (iStart != ((nTotalSamples - 1) / 10)) {
            // compensate for 10% burnin
            sum -= trace.get((nTotalSamples - 1) / 10);
        }
        final int nSamples = nTotalSamples - iStart;
        final int nMaxLag = Math.min(nSamples, MAX_LAG);

        // calculate mean
        final double fMean = sum / nSamples;

        if (iStart != ((nTotalSamples - 1) / 10)) {
            // compensate for 10% burnin
            int iTrace = ((nTotalSamples - 1) / 10);
            for (int iLag = 0; iLag < squareLaggedSums.size(); iLag++) {
                squareLaggedSums.set(iLag, squareLaggedSums.get(iLag) - trace.get(iTrace) * trace.get(iTrace + iLag));
            }
        }

        while (squareLaggedSums.size() < nMaxLag) {
            squareLaggedSums.add(0.0);
        }

        // calculate auto correlation for selected lag times
        double[] fAutoCorrelation = new double[nMaxLag];
        // fSum1 = \sum_{iStart ... nTotalSamples-iLag-1} trace
        double fSum1 = sum;
        // fSum1 = \sum_{iStart+iLag ... nTotalSamples-1} trace
        double fSum2 = sum;
        for (int iLag = 0; iLag < nMaxLag; iLag++) {
            squareLaggedSums.set(iLag, squareLaggedSums.get(iLag) + trace.get(nTotalSamples - iLag - 1) * trace.get(nTotalSamples - 1));
            // The following line is the same approximation as in Tracer 
            // (valid since fMean *(nSamples - iLag), fSum1, and fSum2 are approximately the same)
            // though a more accurate estimate would be
            // fAutoCorrelation[iLag] = m_fSquareLaggedSums.get(iLag) - fSum1 * fSum2
            fAutoCorrelation[iLag] = squareLaggedSums.get(iLag) - (fSum1 + fSum2) * fMean + fMean * fMean * (nSamples - iLag);
            fAutoCorrelation[iLag] /= ((double) (nSamples - iLag));
            fSum1 -= trace.get(nTotalSamples - 1 - iLag);
            fSum2 -= trace.get(iStart + iLag);
        }

        double integralOfACFunctionTimes2 = 0.0;
        for (int iLag = 0; iLag < nMaxLag; iLag++) {
            if (iLag == 0) {
                integralOfACFunctionTimes2 = fAutoCorrelation[0];
            } else if (iLag % 2 == 0) {
                // fancy stopping criterion - see main comment
                if (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag] > 0) {
                    integralOfACFunctionTimes2 += 2.0 * (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag]);
                } else {
                    // stop
                    break;
                }
            }
        }

        // auto correlation time
        final double fACT = integralOfACFunctionTimes2 / fAutoCorrelation[0];

        // effective sample size
        final double fESS = nSamples / fACT;
        String sStr = fESS + "";
        sStr = sStr.substring(0, sStr.indexOf('.') + 2);
        out.print(sStr + "\t");
    } // log

    @Override
    public void close(PrintStream out) {
        // nothing to do
    }


    /**
     * return ESS time of a sample, batch version.
     * Can be used to calculate effective sample size
     *
     * @param fTrace:         values from which the ACT is calculated
     * @param nSampleInterval time between samples *
     */
    public static double calcESS(List<Double> fTrace) {
        return calcESS(fTrace.toArray(new Double[0]), 1);
    }

    public static double calcESS(Double[] fTrace, int nSampleInterval) {
        return fTrace.length / (ACT(fTrace, nSampleInterval) / nSampleInterval);
    }

    public static double ACT(Double[] fTrace, int nSampleInterval) {
        /** sum of trace, excluding burn-in **/
        double fSum = 0.0;
        /** keep track of sums of trace(i)*trace(i_+ lag) for all lags, excluding burn-in  **/
        double[] fSquareLaggedSums = new double[MAX_LAG];
        double[] fAutoCorrelation = new double[MAX_LAG];
        for (int i = 0; i < fTrace.length; i++) {
            fSum += fTrace[i];
            // calculate mean
            final double fMean = fSum / (i + 1);

            // calculate auto correlation for selected lag times
            // fSum1 = \sum_{iStart ... nTotalSamples-iLag-1} trace
            double fSum1 = fSum;
            // fSum1 = \sum_{iStart+iLag ... nTotalSamples-1} trace
            double fSum2 = fSum;
            for (int iLag = 0; iLag < Math.min(i + 1, MAX_LAG); iLag++) {
                fSquareLaggedSums[iLag] = fSquareLaggedSums[iLag] + fTrace[i - iLag] * fTrace[i];
                // The following line is the same approximation as in Tracer
                // (valid since fMean *(nSamples - iLag), fSum1, and fSum2 are approximately the same)
                // though a more accurate estimate would be
                // fAutoCorrelation[iLag] = m_fSquareLaggedSums.get(iLag) - fSum1 * fSum2
                fAutoCorrelation[iLag] = fSquareLaggedSums[iLag] - (fSum1 + fSum2) * fMean + fMean * fMean * (i + 1 - iLag);
                fAutoCorrelation[iLag] /= ((double) (i + 1 - iLag));
                fSum1 -= fTrace[i - iLag];
                fSum2 -= fTrace[iLag];
            }
        }

        final int nMaxLag = Math.min(fTrace.length, MAX_LAG);
        double fIntegralOfACFunctionTimes2 = 0.0;
        for (int iLag = 0; iLag < nMaxLag; iLag++) //{
            if (iLag == 0) //{
                fIntegralOfACFunctionTimes2 = fAutoCorrelation[0];
            else if (iLag % 2 == 0)
                // fancy stopping criterion - see main comment in Tracer code of BEAST 1
                if (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag] > 0) //{
                    fIntegralOfACFunctionTimes2 += 2.0 * (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag]);
                else
                    // stop
                    break;
        //}
        //}
        //}

        // auto correlation time
        return nSampleInterval * fIntegralOfACFunctionTimes2 / fAutoCorrelation[0];
    }

    public static double stdErrorOfMean(Double[] fTrace, int nSampleInterval) {
        /** sum of trace, excluding burn-in **/
        double fSum = 0.0;
        /** keep track of sums of trace(i)*trace(i_+ lag) for all lags, excluding burn-in  **/
        double[] fSquareLaggedSums = new double[MAX_LAG];
        double[] fAutoCorrelation = new double[MAX_LAG];
        for (int i = 0; i < fTrace.length; i++) {
            fSum += fTrace[i];
            // calculate mean
            final double fMean = fSum / (i + 1);

            // calculate auto correlation for selected lag times
            // fSum1 = \sum_{iStart ... nTotalSamples-iLag-1} trace
            double fSum1 = fSum;
            // fSum1 = \sum_{iStart+iLag ... nTotalSamples-1} trace
            double fSum2 = fSum;
            for (int iLag = 0; iLag < Math.min(i + 1, MAX_LAG); iLag++) {
                fSquareLaggedSums[iLag] = fSquareLaggedSums[iLag] + fTrace[i - iLag] * fTrace[i];
                // The following line is the same approximation as in Tracer
                // (valid since fMean *(nSamples - iLag), fSum1, and fSum2 are approximately the same)
                // though a more accurate estimate would be
                // fAutoCorrelation[iLag] = m_fSquareLaggedSums.get(iLag) - fSum1 * fSum2
                fAutoCorrelation[iLag] = fSquareLaggedSums[iLag] - (fSum1 + fSum2) * fMean + fMean * fMean * (i + 1 - iLag);
                fAutoCorrelation[iLag] /= ((double) (i + 1 - iLag));
                fSum1 -= fTrace[i - iLag];
                fSum2 -= fTrace[iLag];
            }
        }

        final int nMaxLag = Math.min(fTrace.length, MAX_LAG);
        double fIntegralOfACFunctionTimes2 = 0.0;
        for (int iLag = 0; iLag < nMaxLag; iLag++) //{
            if (iLag == 0) //{
                fIntegralOfACFunctionTimes2 = fAutoCorrelation[0];
            else if (iLag % 2 == 0)
                // fancy stopping criterion - see main comment in Tracer code of BEAST 1
                if (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag] > 0) //{
                    fIntegralOfACFunctionTimes2 += 2.0 * (fAutoCorrelation[iLag - 1] + fAutoCorrelation[iLag]);
                else
                    // stop
                    break;
        //}
        //}
        //}

        // auto correlation time
        return Math.sqrt(fIntegralOfACFunctionTimes2 / fTrace.length);
    }

} // class ESS

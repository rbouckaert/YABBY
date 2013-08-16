package yabby.core.util;

import java.io.PrintStream;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.YABBYObject;
import yabby.core.Function;
import yabby.core.Input.Validate;
import yabby.core.parameter.BooleanParameter;
import yabby.core.parameter.IntegerParameter;


@Description("calculates sum of a valuable")
public class Sum extends CalculationNode implements Function, Loggable {
    public Input<Function> m_value = new Input<Function>("arg", "argument to be summed", Validate.REQUIRED);

    enum Mode {integer_mode, double_mode}

    Mode m_mode;

    boolean m_bRecompute = true;
    double m_fSum = 0;
    double m_fStoredSum = 0;

    @Override
    public void initAndValidate() {
        Function valuable = m_value.get();
        if (valuable instanceof IntegerParameter || valuable instanceof BooleanParameter) {
            m_mode = Mode.integer_mode;
        } else {
            m_mode = Mode.double_mode;
        }
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getArrayValue() {
        if (m_bRecompute) {
            compute();
        }
        return m_fSum;
    }

    /**
     * do the actual work, and reset flag *
     */
    void compute() {
        m_fSum = 0;
        final Function v = m_value.get();
        for (int i = 0; i < v.getDimension(); i++) {
            m_fSum += v.getArrayValue(i);
        }
        m_bRecompute = false;
    }

    @Override
    public double getArrayValue(int iDim) {
        if (iDim == 0) {
            return getArrayValue();
        }
        return Double.NaN;
    }

    /**
     * CalculationNode methods *
     */
    @Override
    public void store() {
        m_fStoredSum = m_fSum;
        super.store();
    }

    @Override
    public void restore() {
        m_fSum = m_fStoredSum;
        super.restore();
    }

    @Override
    public boolean requiresRecalculation() {
        m_bRecompute = true;
        return true;
    }

    /**
     * Loggable interface implementation follows
     */
    @Override
    public void init(PrintStream out) throws Exception {
        out.print("sum(" + ((YABBYObject) m_value.get()).getID() + ")\t");
    }

    @Override
    public void log(int nSample, PrintStream out) {
        Function valuable = m_value.get();
        final int nDimension = valuable.getDimension();
        double fSum = 0;
        for (int iValue = 0; iValue < nDimension; iValue++) {
            fSum += valuable.getArrayValue(iValue);
        }
        if (m_mode == Mode.integer_mode) {
            out.print((int) fSum + "\t");
        } else {
            out.print(fSum + "\t");
        }
    }

    @Override
    public void close(PrintStream out) {
        // nothing to do
    }

} // class Sum

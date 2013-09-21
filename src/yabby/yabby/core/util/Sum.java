package yabby.core.util;

import java.io.PrintStream;

import yabby.core.YABBYObject;
import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Function;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.Input.Validate;
import yabby.core.parameter.BooleanParameter;
import yabby.core.parameter.IntegerParameter;




@Description("calculates sum of a valuable")
public class Sum extends CalculationNode implements Function, Loggable {
    public Input<Function> functionInput = new Input<Function>("arg", "argument to be summed", Validate.REQUIRED);

    enum Mode {integer_mode, double_mode}

    Mode mode;

    boolean needsRecompute = true;
    double sum = 0;
    double storedSum = 0;

    @Override
    public void initAndValidate() {
        Function valuable = functionInput.get();
        if (valuable instanceof IntegerParameter || valuable instanceof BooleanParameter) {
            mode = Mode.integer_mode;
        } else {
            mode = Mode.double_mode;
        }
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getArrayValue() {
        if (needsRecompute) {
            compute();
        }
        return sum;
    }

    /**
     * do the actual work, and reset flag *
     */
    void compute() {
        sum = 0;
        final Function v = functionInput.get();
        for (int i = 0; i < v.getDimension(); i++) {
            sum += v.getArrayValue(i);
        }
        needsRecompute = false;
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
        storedSum = sum;
        super.store();
    }

    @Override
    public void restore() {
        sum = storedSum;
        super.restore();
    }

    @Override
    public boolean requiresRecalculation() {
        needsRecompute = true;
        return true;
    }

    /**
     * Loggable interface implementation follows
     */
    @Override
    public void init(PrintStream out) throws Exception {
        out.print("sum(" + ((YABBYObject) functionInput.get()).getID() + ")\t");
    }

    @Override
    public void log(int nSample, PrintStream out) {
        Function valuable = functionInput.get();
        final int nDimension = valuable.getDimension();
        double fSum = 0;
        for (int iValue = 0; iValue < nDimension; iValue++) {
            fSum += valuable.getArrayValue(iValue);
        }
        if (mode == Mode.integer_mode) {
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

package yabby.math.statistic;



import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.PrintStream;

import yabby.core.*;
import yabby.core.parameter.Parameter.BaseP;



/**
 * A statistic based on evaluating simple expressions.
 * <p/>
 * The expressions are in RPN, so no parsing issues. whitespace separated. Variables (other statistics),
 * constants and operations. Currently just the basic four, but easy to extend.
 *
 * @author Joseph Heled in beast1, migrated to beast2 by Denise Kuehnert
 */
@Description("RPN calculator to evaluate simple expressions of parameters (Reverse Polish notation is a mathematical notation wherein every operator follows its operands)")
public class RPNcalculator extends CalculationNode implements Loggable, Function {


    public Input<String> strExpressionInput = new Input<String>("expression", "Expressions needed for the calculations", Input.Validate.REQUIRED);
    public Input<List<BaseP>> parametersInput = new Input<List<BaseP>>("parameter", "Parameters needed for the calculations", new ArrayList<BaseP>());

    private RPNexpressionCalculator[] expressions;
    private List<String> names;

    private Map variables;
    RPNexpressionCalculator.GetVariable[] vars;
    int dim;

    public void initAndValidate() throws Exception {

        names = new ArrayList<String>();
        dim = parametersInput.get().get(0).getDimension();

        int pdim;

        for (BaseP p : parametersInput.get()) {

            pdim = p.getDimension();

            if (pdim != dim && dim != 1 && pdim != 1) {
                throw new Exception("error: all parameters have to have same length or be of dimension 1.");
            }
            if (pdim > dim) dim = pdim;

            expressions = new RPNexpressionCalculator[dim];
            names.add(p.toString());

            for (int i = 0; i < pdim; i++) {

                variables = new HashMap<String, Double[]>();

                variables.put(p.getID(), p.getValues());
            }
        }

        vars = new RPNexpressionCalculator.GetVariable[dim];

        for (int i = 0; i < dim; i++) {
            final int index = i;
            vars[i] = new RPNexpressionCalculator.GetVariable() {
                public double get(String name) {
                    Object[] values = ((Object[]) variables.get(name));
                    if (values[0] instanceof Boolean)
                        return ((Boolean) values[values.length > 1 ? index : 0] ? 1. : 0.);
                    if (values[0] instanceof Integer)
                        return (Integer) values[values.length > 1 ? index : 0];
                    return (Double) values[values.length > 1 ? index : 0];
                }
            };
        }

        String err;
        for (int i = 0; i < dim; i++) {
            expressions[i] = new RPNexpressionCalculator(strExpressionInput.get());

            err = expressions[i].validate();
            if (err != null) {
                throw new RuntimeException("Error in expression: " + err);
            }
        }
    }

    private void updateValues() {
        for (BaseP p : parametersInput.get()) {
            for (int i = 0; i < p.getDimension(); i++) {
                variables.put(p.getID(), p.getValues());
            }
        }
    }

    public int getDimension() {
        return dim;
    }


    // todo: add dirty flag to avoid double calculation!!! 
    @Override
    public double getArrayValue() {
        return getStatisticValue(0);
    }

    @Override
    public double getArrayValue(int i) {
        return getStatisticValue(i);
    }

    public String getDimensionName(int dim) {
        return names.get(dim);
    }

    /**
     * @return the value of the expression
     */
    public double getStatisticValue(int i) {
        updateValues();
        return expressions[i].evaluate(vars[i]);
    }


    @Override
    public void init(PrintStream out) throws Exception {
        if (dim == 1)
            out.print(this.getID() + "\t");
        else
            for (int i = 0; i < dim; i++)
                out.print(this.getID() + "_" + i + "\t");
    }

    @Override
    public void log(int nSample, PrintStream out) {
        for (int i = 0; i < dim; i++)
            out.print(getStatisticValue(i) + "\t");
    }

    @Override
    public void close(PrintStream out) {
        // nothing to do
    }

    public List<String> getArguments() {
        List<String> arguments = new ArrayList<String>();
        for (BaseP par : parametersInput.get()) {
            arguments.add(par.getID());
        }
        return arguments;
    }


}
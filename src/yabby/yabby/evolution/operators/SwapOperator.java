package yabby.evolution.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Operator;
import yabby.core.Input.Validate;
import yabby.core.parameter.IntegerParameter;
import yabby.core.parameter.RealParameter;
import yabby.core.parameter.Parameter.BaseP;
import yabby.util.Randomizer;




@Description("A generic operator swapping a one or more pairs in a multi-dimensional parameter")
public class SwapOperator extends Operator {
    public Input<RealParameter> parameterInput = new Input<RealParameter>("parameter", "a real parameter to swap individual values for");
    public Input<IntegerParameter> intparameterInput = new Input<IntegerParameter>("intparameter", "an integer parameter to swap individual values for", Validate.XOR, parameterInput);
    public Input<Integer> howManyInput = new Input<Integer>("howMany", "number of items to swap, default 1, must be less than half the dimension of the parameter", 1);


    int howMany;
    BaseP<?> parameter;
    private List<Integer> masterList = null;

    @Override
    public void initAndValidate() throws Exception {
        if (parameterInput.get() != null) {
            parameter = parameterInput.get();
        } else {
            parameter = intparameterInput.get();
        }

        howMany = howManyInput.get();
        if (howMany * 2 > parameter.getDimension()) {
            throw new Exception("howMany it too large: must be less than half the dimension of the parameter");
        }

        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < parameter.getDimension(); i++) {
            list.add(i);
        }
        masterList = Collections.unmodifiableList(list);
    }

    @Override
    public double proposal() {
        List<Integer> allIndices = new ArrayList<Integer>(masterList);
        int left, right;

        for (int i = 0; i < howMany; i++) {
            left = allIndices.remove(Randomizer.nextInt(allIndices.size()));
            right = allIndices.remove(Randomizer.nextInt(allIndices.size()));
            parameter.swap(left, right);
        }

        return 0.0;
    }

}

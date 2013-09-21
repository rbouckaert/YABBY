package yabby.evolution.tree;



import java.io.PrintStream;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Function;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.Input.Validate;



@Description("Logger to report height of a tree")
public class TreeHeightLogger extends CalculationNode implements Loggable, Function {
    public Input<Tree> treeInput = new Input<Tree>("tree", "tree to report height for.", Validate.REQUIRED);

    @Override
    public void initAndValidate() {
        // nothing to do
    }

    @Override
    public void init(PrintStream out) throws Exception {
        final Tree tree = treeInput.get();
        if (getID() == null || getID().matches("\\s*")) {
            out.print(tree.getID() + ".height\t");
        } else {
            out.print(getID() + "\t");
        }
    }

    @Override
    public void log(int nSample, PrintStream out) {
        final Tree tree = treeInput.get();
        out.print(tree.getRoot().getHeight() + "\t");
    }

    @Override
    public void close(PrintStream out) {
        // nothing to do
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getArrayValue() {
        return treeInput.get().getRoot().getHeight();
    }

    @Override
    public double getArrayValue(int iDim) {
        return treeInput.get().getRoot().getHeight();
    }
}

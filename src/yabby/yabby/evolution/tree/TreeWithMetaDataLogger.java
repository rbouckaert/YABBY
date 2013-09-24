package yabby.evolution.tree;

import java.io.PrintStream;

import yabby.core.YABBYObject;
import yabby.core.Description;
import yabby.core.Function;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.StateNode;
import yabby.core.Input.Validate;
import yabby.core.parameter.Parameter.BaseP;
import yabby.evolution.branchratemodel.BranchRateModel;
import yabby.evolution.tree.Tree.BaseTree;




@Description("Logs tree annotated with metadata and/or rates")
public class TreeWithMetaDataLogger extends YABBYObject implements Loggable {
    public Input<BaseTree> treeInput = new Input<BaseTree>("tree", "tree to be logged", Validate.REQUIRED);
    // TODO: make this input a list of valuables
    public Input<Function> parameterInput = new Input<Function>("metadata", "meta data to be logged with the tree nodes");
    public Input<BranchRateModel.Base> clockModelInput = new Input<BranchRateModel.Base>("branchratemodel", "rate to be logged with branches of the tree");
    public Input<Boolean> substitutionsInput = new Input<Boolean>("substitutions", "report branch lengths as substitutions (branch length times clock rate for the branch)", false);


    String metaDataLabel;
    
    boolean someMetaDataNeedsLogging;
    boolean substitutions = false;

    @Override
    public void initAndValidate() throws Exception {
        if (parameterInput.get() == null && clockModelInput.get() == null) {
        	someMetaDataNeedsLogging = false;
        	return;
            //throw new Exception("At least one of the metadata and branchratemodel inputs must be defined");
        }
    	someMetaDataNeedsLogging = true;
        if (parameterInput.get() != null) {
            metaDataLabel = ((YABBYObject) parameterInput.get()).getID() + "=";
        }
    	// without substitution model, reporting substitutions == reporting branch lengths 
        if (clockModelInput.get() != null) {
        	substitutions = substitutionsInput.get();
        }
    }

    @Override
    public void init(PrintStream out) throws Exception {
        treeInput.get().init(out);
    }

    @Override
    public void log(int nSample, PrintStream out) {
        // make sure we get the current version of the inputs
        BaseTree tree = (BaseTree) treeInput.get().getCurrent();
        Function metadata = parameterInput.get();
        if (metadata != null && metadata instanceof StateNode) {
            metadata = ((StateNode) metadata).getCurrent();
        }
        BranchRateModel.Base branchRateModel = clockModelInput.get();
        // write out the log tree with meta data
        out.print("tree STATE_" + nSample + " = ");
        tree.getRoot().sort();
        out.print(toNewick(tree.getRoot(), metadata, branchRateModel));
        //out.print(tree.getRoot().toShortNewick(false));
        out.print(";");
    }


    String toNewick(Node node, Function metadata, BranchRateModel.Base branchRateModel) {
        StringBuffer buf = new StringBuffer();
        if (node.getLeft() != null) {
            buf.append("(");
            buf.append(toNewick(node.getLeft(), metadata, branchRateModel));
            if (node.getRight() != null) {
                buf.append(',');
                buf.append(toNewick(node.getRight(), metadata, branchRateModel));
            }
            buf.append(")");
        } else {
            buf.append(node.labelNr + 1);
        }
        if (someMetaDataNeedsLogging) {
	        buf.append("[&");
	        if (metadata != null) {
	            buf.append(metaDataLabel);
	            if (metadata instanceof BaseP<?>) {
	            	BaseP p = (BaseP) metadata;
	            	int dim = p.getMinorDimension1();
	            	if (dim > 1) {
		            	buf.append('{');
		            	for (int i = 0; i < dim; i++) {
			            	buf.append(p.getMatrixValue(node.labelNr, i));
			            	if (i < dim - 1) {
				            	buf.append(',');
			            	}
		            	}
		            	buf.append('}');
	            	} else {
		            	buf.append(metadata.getArrayValue(node.labelNr));
	            	}
	            } else {
	            	buf.append(metadata.getArrayValue(node.labelNr));
	            }
	            if (branchRateModel != null) {
	                buf.append(",");
	            }
	        }
	        if (branchRateModel != null) {
	            buf.append("rate=");
	            buf.append(branchRateModel.getRateForBranch(node));
	        }
	        buf.append(']');
        }
        buf.append(":");
        if (substitutions) {
        	buf.append(node.getLength() * branchRateModel.getRateForBranch(node));
        } else {
        	buf.append(node.getLength());
        }
        return buf.toString();
    }


    @Override
    public void close(PrintStream out) {
        treeInput.get().close(out);
    }

}

    
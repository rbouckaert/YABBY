package yabby.evolution.speciation;

import java.io.PrintStream;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.YABBYObject;
import yabby.core.StateNode;
import yabby.core.Function;
import yabby.core.Input.Validate;
import yabby.evolution.speciation.SpeciesTreePrior.PopSizeFunction;
import yabby.evolution.tree.Node;
import yabby.evolution.tree.Tree;


@Description("Logs tree annotated with metadata in starBeast format")
public class SpeciesTreeLogger extends YABBYObject implements Loggable {
    public Input<Tree> m_tree = new Input<Tree>("tree", "tree to be logged", Validate.REQUIRED);
    public Input<Function> m_parameter = new Input<Function>("popSize", "population size parameter associated with tree nodes", Validate.REQUIRED);
    public Input<Function> m_parameterTop = new Input<Function>("popSizeTop", "population size parameter associated with top of tree branches, only used for non-constant *beast analysis");
    public Input<SpeciesTreePrior> speciesTreePrior = new Input<SpeciesTreePrior>("speciesTreePrior", "species tree prior, used to find which Population Size Function is used. If not specified, assumes 'constant'");
    public Input<TreeTopFinder> treeTopFinder = new Input<TreeTopFinder>("treetop", "calculates height of species tree", Validate.REQUIRED);

    PopSizeFunction popSizeFunction;
    String m_sMetaDataLabel;

    static final String dmv = "dmv";
    static final String dmt = "dmt";

    @Override
    public void initAndValidate() {
        m_sMetaDataLabel = "[&" + dmv + "=";
        if (speciesTreePrior.get() != null) {
            popSizeFunction = speciesTreePrior.get().m_popFunctionInput.get();
        } else {
            popSizeFunction = PopSizeFunction.constant;
        }
    }

    @Override
    public void init(final PrintStream out) throws Exception {
        m_tree.get().init(out);
    }

    @Override
    public void log(final int nSample, final PrintStream out) {
        // make sure we get the current version of the inputs
        final Tree tree = (Tree) m_tree.get().getCurrent();
        Function metadata = m_parameter.get();
        if (metadata instanceof StateNode) {
            metadata = ((StateNode) metadata).getCurrent();
        }
        Function metadataTop = m_parameterTop.get();
        if (metadataTop != null && metadataTop instanceof StateNode) {
            metadataTop = ((StateNode) metadataTop).getCurrent();
        }

        // write out the log tree with meta data
        out.print("tree STATE_" + nSample + " = ");
        tree.getRoot().sort();
        out.print(toNewick(tree.getRoot(), metadata, metadataTop));
        //out.print(tree.getRoot().toShortNewick(false));
        out.print(";");
    }


    String toNewick(final Node node, final Function metadata, final Function metadataTop) {
        final StringBuilder buf = new StringBuilder();

        if (node.getLeft() != null) {
            buf.append("(");
            buf.append(toNewick(node.getLeft(), metadata, metadataTop));
            if (node.getRight() != null) {
                buf.append(',');
                buf.append(toNewick(node.getRight(), metadata, metadataTop));
            }
            buf.append(")");
        } else {
            buf.append(node.getNr()+Tree.taxaTranslationOffset);
        }
        buf.append("[&");
        switch (popSizeFunction) {
            case constant: {
                final double popStart = metadata.getArrayValue(node.getNr());
                buf.append(dmv + "=").append(popStart);
                break;
            }
            case linear:
            case linear_with_constant_root:
                buf.append(dmt + "=");
                final double b;
                if (node.isRoot()) {
                    b = treeTopFinder.get().getHighestTreeHeight() - node.getHeight();
                } else {
                    b = node.getLength();
                }
                buf.append(b).append("," + dmv + "={");

                final double popStart;
                if (node.isLeaf()) {
                    popStart = metadata.getArrayValue(node.getNr());
                } else {
                    popStart = (getMetaDataTopValue(node.getLeft(), metadataTop) +
                            getMetaDataTopValue(node.getRight(), metadataTop));
                }
                buf.append(popStart);

                final double popEnd;
                if (node.isRoot() && popSizeFunction == PopSizeFunction.linear_with_constant_root) {
                    popEnd = popStart;
                } else {
                  popEnd = getMetaDataTopValue(node, metadataTop);
                }
                buf.append(",").append(popEnd).append("}");
                break;
        }
        buf.append(']');
        if (!node.isRoot()) {
            buf.append(":").append(node.getLength());
        }
        return buf.toString();
    }

    double getMetaDataTopValue(final Node node, final Function metadataTop) {
        int nr = node.getNr();
        if (nr >= metadataTop.getDimension()) {
            nr = node.getTree().getRoot().getNr();
        }
        return metadataTop.getArrayValue(nr);
    }

    @Override
    public void close(final PrintStream out) {
        m_tree.get().close(out);
    }

}

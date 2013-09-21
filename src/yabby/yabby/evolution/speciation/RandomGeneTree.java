package yabby.evolution.speciation;

import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.evolution.tree.Node;
import yabby.evolution.tree.RandomTree;
import yabby.evolution.tree.Tree;
import yabby.evolution.tree.coalescent.PopulationFunction;




@Description("Generates a random gene tree conditioned on a species tree, such " +
        "that the root of the species tree is lower than any coalescent events in " +
        "the gene tree")
public class RandomGeneTree extends RandomTree {
    public Input<Tree> speciesTreeInput = new Input<Tree>("speciesTree", "The species tree in which this random gene tree needs to fit", Validate.REQUIRED);

    @Override
    public void initAndValidate() throws Exception {
        super.initAndValidate();
    }

    @Override
    public Node simulateCoalescent(List<Node> nodes, PopulationFunction demographic) {
        // sanity check - disjoint trees

//        if( ! Tree.Utils.allDisjoint(nodes) ) {
//            throw new RuntimeException("non disjoint trees");
//        }

        if (nodes.size() == 0) {
            throw new IllegalArgumentException("empty nodes set");
        }

        double fLowestHeight = speciesTreeInput.get().getRoot().getHeight();

        for (int attempts = 0; attempts < 1000; ++attempts) {
            try {
                List<Node> rootNode = simulateCoalescent(nodes, demographic, fLowestHeight, Double.POSITIVE_INFINITY);
                if (rootNode.size() == 1) {
                    return rootNode.get(0);
                }
            } catch (ConstraintViolatedException e) {
                // TODO: handle exception
            }
        }

        throw new RuntimeException("failed to merge trees after 1000 tries!");
    }

}

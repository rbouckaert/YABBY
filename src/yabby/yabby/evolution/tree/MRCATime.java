package yabby.evolution.tree;


import java.io.PrintStream;
import java.util.List;

import yabby.core.CalculationNode;
import yabby.core.Description;
import yabby.core.Function;
import yabby.core.Input;
import yabby.core.Loggable;
import yabby.core.Input.Validate;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.alignment.TaxonSet;




@Description("Calculates the time of the most recent common ancestor (MRCA) for a set of taxa. " +
		"This is useful for adding prior information on sets of taxa to the analysis.")
public class MRCATime extends CalculationNode implements Function, Loggable {
	public Input<TaxonSet> taxaInput = new Input<TaxonSet>("taxa","comma separated list of taxa", Validate.REQUIRED);
	public Input<Alignment> dataInput = new Input<Alignment>("data","alignment containing the complete list of taxa to choose from", Validate.REQUIRED);
	public Input<Tree> treeInput = new Input<Tree>("tree","tree for which the MRCA time is calculated", Validate.REQUIRED);
	
	// number of taxa in taxon set
	int nrOfTaxa = -1;
	// array of flags to indicate which taxa are in the set
	boolean [] isInTaxaSet;
	// stores time to be calculated
	double MRCATime = -1;

	@Override
	public void initAndValidate() throws Exception {
		// determine nr of taxa in taxon set
		Alignment data = dataInput.get();
		List<String> sTaxa = taxaInput.get().asStringList();
		nrOfTaxa = sTaxa.size();
		if (nrOfTaxa <= 1) {
			throw new Exception ("At least two taxa are required in a taxon set");
		}

		// determine which taxa are in the set
		isInTaxaSet = new boolean [data.getNrTaxa()];
		for (String sTaxon : sTaxa) {
			int iTaxon = data.getTaxaNames().indexOf(sTaxon);
			if (iTaxon < 0) {
				throw new Exception ("Cannot find taxon " + sTaxon + " in data");
			}
			if (isInTaxaSet[iTaxon]) {
				throw new Exception ("Taxon " + sTaxon + " is defined multiple times, while they should be unique");
			}
			isInTaxaSet[iTaxon] = true;
		}
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public double getArrayValue() {
		// calculate MRCA time from tree
		Node root = treeInput.get().getRoot();
		calcMRCAtime(root);
		return MRCATime;
	}
	
	/** Recursively visit all leaf nodes, 
	 * and collect number of taxa in the taxon set.
	 * When all taxa in the set are visited, record the time. 
	 * **/
	int calcMRCAtime(Node node) {
		if (node.isLeaf()) {
			if (isInTaxaSet[node.getNr()]) {
				return 1;
			} else {
				return 0;
			}
		} else {
			int iTaxons = calcMRCAtime(node.getLeft());
			if (node.getRight() != null) {
				iTaxons += calcMRCAtime(node.getRight());
				if (iTaxons == nrOfTaxa) {
					// we are at the MRCA, so record the height
					MRCATime = node.getDate();
					return iTaxons + 1;
				}
			}
			return iTaxons;
		}
	}

	@Override
	public double getArrayValue(int iDim) {
		return getArrayValue();
	}

	@Override
	public void init(PrintStream out) throws Exception {
		out.print("mrcatime("+taxaInput.get()+")\t");
	}

	@Override
	public void log(int nSample, PrintStream out) {
		out.print(getArrayValue()+"\t");
	}

	@Override
	public void close(PrintStream out) {
	}

} // class MRCATime

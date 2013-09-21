package yabby.app.beauti;



import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import yabby.app.draw.YABBYObjectPanel;
import yabby.app.draw.InputEditor;
import yabby.app.draw.ListInputEditor;
import yabby.app.draw.SmallButton;
import yabby.core.YABBYObject;
import yabby.core.Distribution;
import yabby.core.Input;
import yabby.core.Logger;
import yabby.core.State;
import yabby.core.StateNode;
import yabby.core.parameter.RealParameter;
import yabby.evolution.alignment.Taxon;
import yabby.evolution.alignment.TaxonSet;
import yabby.evolution.tree.Tree;
import yabby.evolution.tree.TreeDistribution;
import yabby.math.distributions.MRCAPrior;
import yabby.math.distributions.OneOnX;
import yabby.math.distributions.Prior;




public class PriorListInputEditor extends ListInputEditor {
    private static final long serialVersionUID = 1L;

    List<JComboBox> comboBoxes;
    List<JButton> rangeButtons;
    JComboBox currentComboBox;

    List<JButton> taxonButtons;

	public PriorListInputEditor(BeautiDoc doc) {
		super(doc);
	}

    @Override
    public Class<?> type() {
        return List.class;
    }

    @Override
    public Class<?> baseType() {
        return Distribution.class;
    }

    @Override
    public void init(Input<?> input, YABBYObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
    	List<?> list = (List) input.get();
    	Collections.sort(list, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof YABBYObject && o2 instanceof YABBYObject) {
					String sID1 = ((YABBYObject)o1).getID();
					String sID2 = ((YABBYObject)o2).getID();
					// first the tree priors
					if (o1 instanceof TreeDistribution) {
						if (o2 instanceof TreeDistribution) {
							Tree tree1 = ((TreeDistribution)o1).treeInput.get();
							if (tree1 == null) {
								tree1 = ((TreeDistribution)o1).treeIntervalsInput.get().treeInput.get();
							}
							Tree tree2 = ((TreeDistribution)o2).treeInput.get();
							if (tree2 == null) {
								tree2 = ((TreeDistribution)o2).treeIntervalsInput.get().treeInput.get();
							}
							return sID1.compareTo(sID2);
						} else {
							return -1;
						}
					} else if (o1 instanceof MRCAPrior) {
						// last MRCA priors
						if (o2 instanceof MRCAPrior) {
							return sID1.compareTo(sID2);
						} else {
							return 1;
						}
					} else {
						if (o2 instanceof TreeDistribution) {
							return 1;
						}
						if (o2 instanceof MRCAPrior) {
							return -1;
						}
						if (o1 instanceof Prior) {
							sID1 = ((Prior) o1).getParameterName(); 
						}
						if (o2 instanceof Prior) {
							sID2 = ((Prior) o2).getParameterName(); 
						}
						return sID1.compareTo(sID2);
					}
				}
				return 0;
			}
		});
    	
    	
        comboBoxes = new ArrayList<JComboBox>();
        rangeButtons = new ArrayList<JButton>();
        taxonButtons = new ArrayList<JButton>();
        
        //m_buttonStatus = ButtonStatus.NONE;
        super.init(input, plugin, itemNr, bExpandOption, bAddButtons);

        
        if (m_buttonStatus == ButtonStatus.ALL || m_buttonStatus == ButtonStatus.ADD_ONLY) {
	        m_addButton = new SmallButton("+", true);
	        m_addButton.setName("addItem");
	        m_addButton.setToolTipText("Add item to the list");
	        m_addButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                addItem();
	            }
	        });
	        add(m_addButton);
        }
    }


    /**
     * add components to box that are specific for the plugin.
     * By default, this just inserts a label with the plugin ID
     *
     * @param itemBox box to add components to
     * @param plugin  plugin to add
     */
    @Override
    protected InputEditor addPluginItem(Box itemBox, YABBYObject plugin) {
		try {
	    	int listItemNr = ((List) m_input.get()).indexOf(plugin);
	    	InputEditor editor = doc.getInpuEditorFactory().createInputEditor(m_input, listItemNr, plugin, false, ExpandOption.FALSE, ButtonStatus.NONE, null, doc);
	    	itemBox.add((Component) editor);
	    	return editor;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
    }	


    String paramToString(RealParameter p) {
        Double lower = p.lowerValueInput.get();
        Double upper = p.upperValueInput.get();
        return "initial = " + p.valuesInput.get() +
                " [" + (lower == null ? "-\u221E" : lower + "") +
                "," + (upper == null ? "\u221E" : upper + "") + "]";
    }

    Set<Taxon> getTaxonCandidates(MRCAPrior prior) {
        Set<Taxon> candidates = new HashSet<Taxon>();
        Tree tree = prior.treeInput.get();
        String [] taxa = null;
        if (tree.m_taxonset.get() != null) {
        	try {
            	TaxonSet set = tree.m_taxonset.get();
        		set.initAndValidate();
            	taxa = set.asStringList().toArray(new String[0]);
        	} catch (Exception e) {
            	taxa = prior.treeInput.get().getTaxaNames();
			}
        } else {
        	taxa = prior.treeInput.get().getTaxaNames();
        }
        
        
        for (String sTaxon : taxa) {
            Taxon taxon = null;
            for (Taxon taxon2 : doc.taxaset) {
                if (taxon2.getID().equals(sTaxon)) {
                    taxon = taxon2;
                    break;
                }
            }
            if (taxon == null) {
                taxon = new Taxon();
                taxon.setID(sTaxon);
                doc.taxaset.add(taxon);
            }
            candidates.add(taxon);
        }
        return candidates;
    }

    /**
     * class to deal with toggling monophyletic flag on an MRCAPrior *
     */
    class MRCAPriorActionListener implements ActionListener {
        MRCAPrior m_prior;

        MRCAPriorActionListener(MRCAPrior prior) {
            m_prior = prior;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                m_prior.isMonophyleticInput.setValue(((JCheckBox) e.getSource()).isSelected(), m_prior);
                refreshPanel();
            } catch (Exception ex) {
                System.err.println("PriorListInputEditor " + ex.getMessage());
            }
        }
    }

    @Override
    protected void addItem() {
        super.addItem();
        sync();
        refreshPanel();
    } // addItem

    @Override
    public List<YABBYObject> pluginSelector(Input<?> input, YABBYObject parent, List<String> sTabuList) {
        MRCAPrior prior = new MRCAPrior();
        try {

            List<Tree> trees = new ArrayList<Tree>();
            getDoc().scrubAll(true, false);
            State state = (State) doc.pluginmap.get("state");
            for (StateNode node : state.stateNodeInput.get()) {
                if (node instanceof Tree) { // && ((Tree) node).m_initial.get() != null) {
                    trees.add((Tree) node);
                }
            }
            int iTree = 0;
            if (trees.size() > 1) {
                String[] sTreeIDs = new String[trees.size()];
                for (int j = 0; j < sTreeIDs.length; j++) {
                    sTreeIDs[j] = trees.get(j).getID();
                }
                iTree = JOptionPane.showOptionDialog(null, "Select a tree", "MRCA selector",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        sTreeIDs, trees.get(0));
            }
            if (iTree < 0) {
                return null;
            }
            prior.treeInput.setValue(trees.get(iTree), prior);
            TaxonSet taxonSet = new TaxonSet();

            TaxonSetDialog dlg = new TaxonSetDialog(taxonSet, getTaxonCandidates(prior), doc);
            if (!dlg.showDialog() || dlg.taxonSet.getID() == null) {
                return null;
            }
            taxonSet = dlg.taxonSet;
            YABBYObjectPanel.addPluginToMap(taxonSet, doc);
            prior.taxonsetInput.setValue(taxonSet, prior);
            prior.setID(taxonSet.getID() + ".prior");
            // this sets up the type
            prior.distInput.setValue(new OneOnX(), prior);
            // this removes the parametric distribution
            prior.distInput.setValue(null, prior);

            Logger logger = (Logger) doc.pluginmap.get("tracelog");
            logger.loggersInput.setValue(prior, logger);
        } catch (Exception e) {
            // TODO: handle exception
        }
        List<YABBYObject> selectedPlugins = new ArrayList<YABBYObject>();
        selectedPlugins.add(prior);
        g_collapsedIDs.add(prior.getID());
        return selectedPlugins;
    }
}

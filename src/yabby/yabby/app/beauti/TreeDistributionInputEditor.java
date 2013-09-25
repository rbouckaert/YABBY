package yabby.app.beauti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import yabby.app.draw.InputEditor;
import yabby.app.draw.SmallLabel;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.evolution.tree.Tree;
import yabby.evolution.tree.TraitSet;
import yabby.evolution.tree.TreeDistribution;



//import beast.evolution.speciation.BirthDeathGernhard08Model;
//import beast.evolution.speciation.YuleModel;

public class TreeDistributionInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;

	public TreeDistributionInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return TreeDistribution.class;
	}
	
//	@Override
//	public Class<?>[] types() {
//		ArrayList<Class> types = new ArrayList<Class>();
//		types.add(TreeDistribution.class);
//		types.add(BirthDeathGernhard08Model.class);
//		types.add(YuleModel.class);
//		types.add(Coalescent.class);
//		types.add(BayesianSkyline.class);
//		return types.toArray(new Class[0]);
//	}

	ActionEvent m_e;
	
	@Override
	public void init(Input<?> input, YABBYObject plugin, int listItemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		m_bAddButtons = bAddButtons;
		m_input = input;
		m_plugin = plugin;
		this.itemNr = listItemNr;

		Box itemBox = Box.createHorizontalBox();

		TreeDistribution distr = (TreeDistribution) plugin;
		String sText = ""/* plugin.getID() + ": " */;
		if (distr.treeInput.get() != null) {
			sText += distr.treeInput.get().getID();
		} else {
			sText += distr.treeIntervalsInput.get().treeInput.get().getID();
		}
		JLabel label = new JLabel(sText);
		label.setMinimumSize(PriorListInputEditor.PREFERRED_SIZE);
		label.setPreferredSize(PriorListInputEditor.PREFERRED_SIZE);
		itemBox.add(label);
		// List<String> sAvailablePlugins =
		// PluginPanel.getAvailablePlugins(m_input, m_plugin, null);

		List<BeautiSubTemplate> sAvailablePlugins = doc.getInpuEditorFactory().getAvailableTemplates(m_input, m_plugin,
				null, doc);
		JComboBox comboBox = new JComboBox(sAvailablePlugins.toArray());
		comboBox.setName("TreeDistribution");

		for (int i = sAvailablePlugins.size() - 1; i >= 0; i--) {
			if (!TreeDistribution.class.isAssignableFrom(sAvailablePlugins.get(i)._class)) {
				sAvailablePlugins.remove(i);
			}
		}

		String sID = distr.getID();
		try {
			// sID = BeautiDoc.parsePartition(sID);
			sID = sID.substring(0, sID.indexOf('.'));
		} catch (Exception e) {
			throw new RuntimeException("Improperly formatted ID: " + distr.getID());
		}
		for (BeautiSubTemplate template : sAvailablePlugins) {
			if (template.matchesName(sID)) { // getMainID().replaceAll(".\\$\\(n\\)",
												// "").equals(sID)) {
				comboBox.setSelectedItem(template);
			}
		}

		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_e = e;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JComboBox currentComboBox = (JComboBox) m_e.getSource();
						@SuppressWarnings("unchecked")
						List<YABBYObject> list = (List<YABBYObject>) m_input.get();
						BeautiSubTemplate template = (BeautiSubTemplate) currentComboBox.getSelectedItem();
						PartitionContext partitionContext = doc.getContextFor((YABBYObject) list.get(itemNr));
						try {
							template.createSubNet(partitionContext, list, itemNr, true);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						sync();
						refreshPanel();
					}

				});
			}
		});
		itemBox.add(comboBox);
        itemBox.add(Box.createGlue());

        m_validateLabel = new SmallLabel("x", new Color(200, 0, 0));
        m_validateLabel.setVisible(false);
        validateInput();
        itemBox.add(m_validateLabel);
		add(itemBox);
	}

	@Override
	public void validateInput() {
		TreeDistribution distr = (TreeDistribution) m_plugin;
	    Tree tree = distr.treeInput.get();
	    if (tree == null) {
	    	tree = distr.treeIntervalsInput.get().treeInput.get();
	    }
        if (tree.m_trait.get() != null) {
        	String traitName = tree.m_trait.get().traitNameInput.get();
        	if (traitName.equals(TraitSet.DATE_TRAIT) ||
        		traitName.equals(TraitSet.DATE_BACKWARD_TRAIT) ||
        		traitName.equals(TraitSet.DATE_FORWARD_TRAIT)) {
	        		if (!distr.canHandleTipDates()) {
	                    m_validateLabel.setToolTipText("This tree prior cannot handle dated tips. Choose another tree prior.");
	                    m_validateLabel.m_circleColor = Color.red;
	                    m_validateLabel.setVisible(true);
	                    return;
	        		}
        	}
        }


		super.validateInput();
	}

}

package yabby.app.beauti;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import yabby.app.beauti.PriorListInputEditor.MRCAPriorActionListener;
import yabby.app.draw.InputEditor;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.evolution.alignment.Taxon;
import yabby.evolution.alignment.TaxonSet;
import yabby.math.distributions.MRCAPrior;
import yabby.math.distributions.OneOnX;




public class MRCAPriorInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;

	public MRCAPriorInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return MRCAPrior.class;
	}

	@Override
	public void init(Input<?> input, YABBYObject plugin, final int listItemNr, ExpandOption bExpandOption, boolean bAddButtons) {
        m_bAddButtons = bAddButtons;
        m_input = input;
        m_plugin = plugin;
        this.itemNr= listItemNr;
		
        Box itemBox = Box.createHorizontalBox();

        MRCAPrior prior = (MRCAPrior) plugin;
        String sText = prior.getID();

        JButton taxonButton = new JButton(sText);
        taxonButton.setMinimumSize(PriorInputEditor.PREFERRED_SIZE);
        taxonButton.setPreferredSize(PriorInputEditor.PREFERRED_SIZE);
        itemBox.add(taxonButton);
        taxonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton taxonButton = (JButton) e.getSource();
                List<?> list = (List<?>) m_input.get();
                MRCAPrior prior = (MRCAPrior) list.get(itemNr);
                try {
                    TaxonSet taxonset = prior.taxonsetInput.get();
                    Set<Taxon> candidates = getTaxonCandidates(prior);
                    TaxonSetDialog dlg = new TaxonSetDialog(taxonset, candidates, doc);
                    if (dlg.showDialog()) {
                        prior.setID(dlg.taxonSet.getID());
                        prior.taxonsetInput.setValue(dlg.taxonSet, prior);
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                refreshPanel();
            }
        });


        if (prior.distInput.getType() == null) {
            try {
                prior.distInput.setValue(new OneOnX(), prior);
                prior.distInput.setValue(null, prior);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        List<BeautiSubTemplate> sAvailablePlugins = doc.getInpuEditorFactory().getAvailableTemplates(prior.distInput, prior, null, doc);
        JComboBox comboBox = new JComboBox(sAvailablePlugins.toArray());
        comboBox.setName(sText+".distr");

        if (prior.distInput.get() != null) {
            String sID = prior.distInput.get().getID();
            //sID = BeautiDoc.parsePartition(sID);
            sID = sID.substring(0, sID.indexOf('.'));
            for (BeautiSubTemplate template : sAvailablePlugins) {
                if (template.sClassInput.get() != null && template.sShortClassName.equals(sID)) {
                    comboBox.setSelectedItem(template);
                }
            }
        } else {
            comboBox.setSelectedItem(BeautiConfig.NULL_TEMPLATE);
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                BeautiSubTemplate template = (BeautiSubTemplate) comboBox.getSelectedItem();
                List<?> list = (List<?>) m_input.get();
                MRCAPrior prior = (MRCAPrior) list.get(itemNr);

//System.err.println("PRIOR" + plugin2);
//            	try {
//					prior.m_distInput.setValue(plugin2, prior);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
                try {
                    //Plugin plugin2 =
                    template.createSubNet(new PartitionContext(""), prior, prior.distInput, true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                refreshPanel();
            }
        });
        itemBox.add(comboBox);

        JCheckBox isMonophyleticdBox = new JCheckBox(doc.beautiConfig.getInputLabel(prior, prior.isMonophyleticInput.getName()));
        isMonophyleticdBox.setName(sText+".isMonophyletic");
        isMonophyleticdBox.setSelected(prior.isMonophyleticInput.get());
        isMonophyleticdBox.setToolTipText(prior.isMonophyleticInput.getTipText());
        isMonophyleticdBox.addActionListener(new MRCAPriorActionListener(prior));
        itemBox.add(isMonophyleticdBox);
        itemBox.add(Box.createGlue());

        add(itemBox);
	}
	
    Set<Taxon> getTaxonCandidates(MRCAPrior prior) {
        Set<Taxon> candidates = new HashSet<Taxon>();
        for (String sTaxon : prior.treeInput.get().getTaxaNames()) {
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

}

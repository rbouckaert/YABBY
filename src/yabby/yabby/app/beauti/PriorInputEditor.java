package yabby.app.beauti;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import yabby.app.draw.YABBYbjectDialog;
import yabby.app.draw.InputEditor;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.core.parameter.RealParameter;
import yabby.math.distributions.Prior;




public class PriorInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;

	public PriorInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return Prior.class;
	}

	@Override
	public void init(Input<?> input, YABBYObject plugin, int listItemNr, ExpandOption bExpandOption, boolean bAddButtons) {
        m_bAddButtons = bAddButtons;
        m_input = input;
        m_plugin = plugin;
        this.itemNr= listItemNr;
		
        Box itemBox = Box.createHorizontalBox();

        Prior prior = (Prior) plugin;
        String sText = prior.getParameterName();
        JLabel label = new JLabel(sText);
        label.setMinimumSize(PREFERRED_SIZE);
        label.setPreferredSize(PREFERRED_SIZE);
        itemBox.add(label);


        List<BeautiSubTemplate> sAvailablePlugins = doc.getInpuEditorFactory().getAvailableTemplates(prior.distInput, prior, null, doc);
        JComboBox comboBox = new JComboBox(sAvailablePlugins.toArray());
        comboBox.setName(sText+".distr");

        String sID = prior.distInput.get().getID();
        System.err.println("id=" + sID);
        sID = sID.substring(0, sID.indexOf('.'));
        for (BeautiSubTemplate template : sAvailablePlugins) {
            if (template.sClassInput.get() != null && template.sShortClassName.equals(sID)) {
                comboBox.setSelectedItem(template);
            }
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();

                List<?> list = (List<?>) m_input.get();

                BeautiSubTemplate template = (BeautiSubTemplate) comboBox.getSelectedItem();
                //String sID = ((Plugin) list.get(iItem)).getID();
                //String sPartition = BeautiDoc.parsePartition(sID);
                PartitionContext context = doc.getContextFor((YABBYObject) list.get(itemNr));
                Prior prior = (Prior) list.get(itemNr);
                try {
                    template.createSubNet(context, prior, prior.distInput, true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                sync();
                refreshPanel();
            }
        });
        itemBox.add(comboBox);

        if (prior.m_x.get() instanceof RealParameter) {
            // add range button for real parameters
            RealParameter p = (RealParameter) prior.m_x.get();
            JButton rangeButton = new JButton(paramToString(p));
            rangeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton rangeButton = (JButton) e.getSource();

                    List<?> list = (List<?>) m_input.get();
                    Prior prior = (Prior) list.get(itemNr);
                    RealParameter p = (RealParameter) prior.m_x.get();
                    YABBYbjectDialog dlg = new YABBYbjectDialog(p, RealParameter.class, doc);
                    if (dlg.showDialog()) {
                        dlg.accept(p, doc);
                        rangeButton.setText(paramToString(p));
                        refreshPanel();
                    }
                }
            });
            itemBox.add(Box.createHorizontalStrut(10));
            itemBox.add(rangeButton);
        }
        comboBox.setMaximumSize(new Dimension(1024, 24));

        String sTipText = getDoc().tipTextMap.get(plugin.getID());
        System.out.println(plugin.getID());
        if (sTipText != null) {
            JLabel tipTextLabel = new JLabel(" " + sTipText);
            itemBox.add(tipTextLabel);
        }
        itemBox.add(Box.createGlue());

        add(itemBox);
	}

    String paramToString(RealParameter p) {
        Double lower = p.lowerValueInput.get();
        Double upper = p.upperValueInput.get();
        return "initial = " + p.valuesInput.get() +
                " [" + (lower == null ? "-\u221E" : lower + "") +
                "," + (upper == null ? "\u221E" : upper + "") + "]";
    }

}

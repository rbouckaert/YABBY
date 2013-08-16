package yabby.app.beauti;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import yabby.app.draw.PluginInputEditor;
import yabby.core.Input;
import yabby.core.YABBYObject;
import yabby.core.parameter.RealParameter;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.substitutionmodel.Frequencies;


public class FrequenciesInputEditor extends PluginInputEditor {
    RealParameter freqsParameter;
    Alignment alignment;

    private static final long serialVersionUID = 1L;
    boolean bUseDefaultBehavior;

	public FrequenciesInputEditor(BeautiDoc doc) {
		super(doc);
	}

    @Override
    public Class<?> type() {
        return ActionEvent.class;
        //return Frequencies.class;
    }

    @Override
    public void init(Input<?> input, YABBYObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
        super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
    } // init


    @Override
    /** suppress combobox **/
    protected void addComboBox(JComponent box, Input<?> input, YABBYObject plugin) {
        Frequencies freqs = (Frequencies) input.get();

        JComboBox comboBox = new JComboBox(new String[]{"Estimated", "Empirical", "All equal"});
        if (freqs.frequencies.get() != null) {
            comboBox.setSelectedIndex(0);
            freqsParameter = freqs.frequencies.get();
            alignment = (Alignment) getCandidate(freqs.m_data, freqs);
        } else if (freqs.m_bEstimate.get()) {
            comboBox.setSelectedIndex(1);
            alignment = freqs.m_data.get();
            freqsParameter = (RealParameter) getCandidate(freqs.frequencies, freqs);
        } else {
            comboBox.setSelectedIndex(2);
            alignment = freqs.m_data.get();
            freqsParameter = (RealParameter) getCandidate(freqs.frequencies, freqs);
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                int iSelected = comboBox.getSelectedIndex();
                Frequencies freqs = (Frequencies) m_input.get();
                try {
                    switch (iSelected) {
                        case 0:
                            freqs.frequencies.setValue(freqsParameter, freqs);
                            freqs.m_data.setValue(null, freqs);
                            break;
                        case 1:
                            freqs.frequencies.setValue(null, freqs);
                            freqs.m_data.setValue(alignment, freqs);
                            freqs.m_bEstimate.setValue(true, freqs);
                            break;
                        case 2:
                            freqs.frequencies.setValue(null, freqs);
                            freqs.m_data.setValue(alignment, freqs);
                            freqs.m_bEstimate.setValue(false, freqs);
                            break;
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                //System.err.println(freqs.frequencies.get() + " " + freqs.m_data.get() + " " + freqs.m_bEstimate.get());
            }
        });
        box.add(comboBox);
    }

    private YABBYObject getCandidate(Input<?> input, Frequencies freqs) {
        return getDoc().getPartition(freqs);
//		List<String> sCandidates = PluginPanel.getAvailablePlugins(input, freqs, null);
//		String sID = sCandidates.get(0);
//		Plugin plugin = PluginPanel.g_plugins.get(sID);
//		return plugin;
    }


    @Override
    /** suppress input label**/
    protected void addInputLabel() {
        super.addInputLabel();
    }

}

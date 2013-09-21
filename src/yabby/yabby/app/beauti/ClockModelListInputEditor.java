package yabby.app.beauti;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import yabby.app.draw.ListInputEditor;
import yabby.app.draw.SmallLabel;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.core.MCMC;
import yabby.core.Operator;
import yabby.core.parameter.IntegerParameter;
import yabby.core.parameter.RealParameter;
import yabby.evolution.alignment.Alignment;
import yabby.evolution.branchratemodel.BranchRateModel;
import yabby.evolution.operators.DeltaExchangeOperator;




public class ClockModelListInputEditor extends ListInputEditor {
    private static final long serialVersionUID = 1L;
    List<JTextField> textFields = new ArrayList<JTextField>();
    List<Operator> operators = new ArrayList<Operator>();

	public ClockModelListInputEditor(BeautiDoc doc) {
		super(doc);
	}

    @Override
    public Class<?> type() {
        return List.class;
    }

    @Override
    public Class<?> baseType() {
    	// disable this editor
    	return ClockModelListInputEditor.class;
        //return BranchRateModel.Base.class;
    }

    JCheckBox fixMeanRatesCheckBox;
    
    DeltaExchangeOperator operator;
    protected SmallLabel fixMeanRatesValidateLabel;
    
    @Override
    public void init(Input<?> input, YABBYObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
    	fixMeanRatesCheckBox = new JCheckBox("Fix mean rate of clock models");
    	m_buttonStatus = ButtonStatus.NONE;
    	super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
    	
		List<Operator> operators = ((MCMC) doc.mcmc.get()).operatorsInput.get();
    	fixMeanRatesCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox averageRatesBox = (JCheckBox) e.getSource();
				boolean averageRates = averageRatesBox.isSelected();
				List<Operator> operators = ((MCMC) doc.mcmc.get()).operatorsInput.get();
				if (averageRates) {
					// connect DeltaExchangeOperator
					if (!operators.contains(operator)) {
						operators.add(operator);
					}
					// set up relative weights
					setUpOperator();
				} else {
					operators.remove(operator);
					fixMeanRatesValidateLabel.setVisible(false);
					repaint();
				}
			}

		});
    	operator = (DeltaExchangeOperator) doc.pluginmap.get("FixMeanRatesOperator");
    	if (operator == null) {
    		operator = new DeltaExchangeOperator();
    		try {
    			operator.setID("FixMeanRatesOperator");
				operator.initByName("weight", 2.0, "delta", 0.75);
			} catch (Exception e1) {
				// ignore initAndValidate exception
			}
    		doc.addPlugin(operator);
    	}
		fixMeanRatesCheckBox.setSelected(operators.contains(operator));
		Box box = Box.createHorizontalBox();
		box.add(fixMeanRatesCheckBox);
		box.add(Box.createHorizontalGlue());
		fixMeanRatesValidateLabel = new SmallLabel("x", Color.GREEN);
		fixMeanRatesValidateLabel.setVisible(false);
		box.add(fixMeanRatesValidateLabel);
		
    	if (((List<?>) input.get()).size() > 1 && operator != null) {
    		add(box);
    	}
		setUpOperator();
    }
    
    @Override
    public void validateInput() {
    	super.validateInput();
    	System.err.println("validateInput()");
    }
    
    /** set up relative weights and parameter input **/
    private void setUpOperator() {
    	String weights = "";
    	List<RealParameter> parameters = operator.parameterInput.get();
    	parameters.clear();
    	double commonClockRate = -1;
    	boolean bAllClocksAreEqual = true;
		try {
	    	for (int i = 0; i < doc.alignments.size(); i++) {
	    		Alignment data = doc.alignments.get(i); 
	    		int weight = data.getSiteCount();
	    		BranchRateModel.Base clockModel = (BranchRateModel.Base) doc.clockModels.get(i);
	    		RealParameter clockRate = clockModel.meanRateInput.get();
	    		//clockRate.m_bIsEstimated.setValue(true, clockRate);
	    		if (clockRate.isEstimatedInput.get()) {
	    			if (commonClockRate < 0) {
	    				commonClockRate = Double.parseDouble(clockRate.valuesInput.get());
	    			} else {
	    				if (Math.abs(commonClockRate - Double.parseDouble(clockRate.valuesInput.get())) > 1e-10) {
	    					bAllClocksAreEqual = false;
	    				}
	    			}
    				weights += weight + " ";
	    			parameters.add(clockRate);
	    		}
	    		//doc.bAutoSetClockRate = false;
	    	}
	    	if (!fixMeanRatesCheckBox.isSelected()) {
	    		fixMeanRatesValidateLabel.setVisible(false);
	    		return;
	    	}
	    	if (parameters.size() == 0) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.m_circleColor = Color.red;
	    		fixMeanRatesValidateLabel.setToolTipText("The model is invalid: At least one clock rate should be estimated.");
	    		return;
	    	}

	    	IntegerParameter weightParameter = new IntegerParameter(weights);
			weightParameter.setID("weightparameter");
			weightParameter.isEstimatedInput.setValue(false, weightParameter);
	    	operator.parameterWeightsInput.setValue(weightParameter, operator);
	    	if (!bAllClocksAreEqual) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.m_circleColor = Color.orange;
	    		fixMeanRatesValidateLabel.setToolTipText("Not all clocks are equal. Are you sure this is what you want?");
	    	} else if (parameters.size() == 1) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.m_circleColor = Color.orange;
	    		fixMeanRatesValidateLabel.setToolTipText("At least 2 clock models should have their rate estimated");
	    	} else if (parameters.size() < doc.alignments.size()) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.m_circleColor = Color.orange;
	    		fixMeanRatesValidateLabel.setToolTipText("Not all partitions have their rate estimated");
	    	} else {
	    		fixMeanRatesValidateLabel.setVisible(false);
	    	}
			repaint();
    		//doc.bAutoSetClockRate = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

} // OperatorListInputEditor

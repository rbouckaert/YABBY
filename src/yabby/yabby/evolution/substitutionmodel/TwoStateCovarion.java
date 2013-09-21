package yabby.evolution.substitutionmodel;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.core.parameter.RealParameter;
import yabby.evolution.datatype.DataType;


@Description("Covarion model for binary data")
public class TwoStateCovarion extends GeneralSubstitutionModel {

	public Input<RealParameter> alphaInput = new Input<RealParameter>("alpha","alpha parameter, the muatation rate between state zero and one", Validate.REQUIRED);
	public Input<RealParameter> switchingParameterInput = new Input<RealParameter>("switchingParameter","switchingParameter parameter, the rate between stable 0,1 and unstable 0,1 states", Validate.REQUIRED);
	
	/** we don't need the m_rates input for TwoStateCovarion **/
	public TwoStateCovarion() {
		ratesInput.setRule(Validate.OPTIONAL);
	}
	
    @Override
    public void initAndValidate() throws Exception {
    	frequencies = frequenciesInput.get();
        updateMatrix = true;
        nrOfStates = frequencies.getFreqs().length;
        relativeRates = new double[12];
        storedRelativeRates = new double[12];
        eigenSystem = createEigenSystem();
        rateMatrix = new double[nrOfStates][nrOfStates];
    }

    @Override
    protected void setupRelativeRates() {
    	relativeRates[0] = alphaInput.get().getValue();
        relativeRates[1] = switchingParameterInput.get().getValue();
        relativeRates[2] = 0.0;

        relativeRates[3] = relativeRates[0];
        relativeRates[4] = 0.0;
        relativeRates[5] = relativeRates[1];//m_switchingParameter.get().getValue();

        relativeRates[6] = relativeRates[1];
        relativeRates[7] = relativeRates[4];
        relativeRates[8] = 1.0;

        relativeRates[9] = relativeRates[2];
        relativeRates[10] = relativeRates[5];
        relativeRates[11] = relativeRates[8];
    }	

	@Override
	public boolean canHandleDataType(DataType dataType) throws Exception {
		if (dataType.getStateCount() == 4) {
			return true;
		}
		throw new Exception("Can only handle 4 state data");
	}
} // class TwoStateCovarion

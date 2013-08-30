package yabby.evolution.datatype;

import yabby.core.Description;
import yabby.evolution.datatype.DataType.Base;

@Description("Datatype for integer sequences")
public class IntegerData extends Base {

    public IntegerData() {
        stateCount = -1;
        mapCodeToStateSet = null;
        codeLength = -1;
        codeMap = null;
    }

    @Override
    public String getDescription() {
        return "integer";
    }
    
    @Override
    public boolean isAmbiguousState(int state) {
    	return state < 0;
    }
    
    @Override
    public char getChar(int state) {
    	if (state < 0) {
    		return '?';
    	}
        return (char)('0'+state);
    }

}

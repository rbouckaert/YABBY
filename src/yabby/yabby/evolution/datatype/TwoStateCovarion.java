package yabby.evolution.datatype;

import yabby.core.Description;
import yabby.evolution.datatype.DataType.Base;

@Description("Datatype for two state covarion sequences")
public class TwoStateCovarion extends Base {
    int[][] x = {
            {0, 2},  // 0
            {1, 3},  // 1
            {0},  // a
            {1},  // b
            {2},  // c
            {3},  // d
            {0, 1, 2, 3},  // -
            {0, 1, 2, 3},  // ?
    };

    public TwoStateCovarion() {
        stateCount = 4;
        mapCodeToStateSet = x;
        codeLength = 1;
        codeMap = "01abcd" + GAP_CHAR + MISSING_CHAR;
    }

    @Override
    public String getDescription() {
        return "twoStateCovarion";
    }

    @Override
    public char getChar(int state) {
        return codeMap.charAt(state);
    }
}

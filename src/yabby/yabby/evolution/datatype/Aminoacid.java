package yabby.evolution.datatype;

import yabby.core.Description;
import yabby.evolution.datatype.DataType.Base;

@Description("DataType for amino acids.")
public class Aminoacid extends Base {

    public Aminoacid() {
        stateCount = 20;
        codeLength = 1;
        codeMap = "ACDEFGHIKLMNPQRSTVWY" + GAP_CHAR + MISSING_CHAR;

        mapCodeToStateSet = new int[22][];
        for (int i = 0; i < 20; i++) {
            mapCodeToStateSet[i] = new int[1];
            mapCodeToStateSet[i][0] = i;
        }
        int[] all = new int[20];
        for (int i = 0; i < 20; i++) {
            all[i] = i;
        }
        mapCodeToStateSet[20] = all;
        mapCodeToStateSet[21] = all;
    }

    @Override
    public String getDescription() {
        return "aminoacid";
    }

}

package org.redstonechips.basiccircuits;

import org.redstonechips.chip.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class encoder extends Circuit {

    @Override
    public void input(boolean state, int inIdx) {
        if (BooleanArrays.isZero(inputs)) writeBits(inputs);
        else {
            writeInt(0, outputlen, inputs.length-1);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen > Math.pow(2, outputlen)) 
            return error("Number of inputs must be no more than 2 to the power of the number of outputs.");
        else 
            return this;
    }
}

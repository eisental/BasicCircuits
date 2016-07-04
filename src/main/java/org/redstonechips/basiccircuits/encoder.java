package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class encoder extends Circuit {

    @Override
    public void input(boolean state, int inIdx) {
        int totalEnabledBits = 0;
        int mostSignificantBit = -1;
        
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i]) {
                totalEnabledBits++;
                mostSignificantBit = i;
            }
        }
        
        if (totalEnabledBits == 1) {
            writeInt(mostSignificantBit, 0, outputlen);
        }
        else {
            this.clearOutputs();
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

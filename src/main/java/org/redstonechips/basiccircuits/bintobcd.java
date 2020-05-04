package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class bintobcd extends Circuit {
    int digits;

    @Override
    public void input(boolean state, int inIdx) {
        String value;
        value = Long.toString(BooleanArrays.toUnsignedInt(inputs, 0, inputlen));
        for (int i=0; i<digits; i++) {
            String d;
            int digit;         
            if (i<value.length()) {
                d = Character.toString(value.charAt(value.length()-i-1));
                digit = Integer.decode(d);
            } else {
                digit = 0;
            }
            this.writeInt(digit, i*4, 4);
        }
    }

    @Override
    public Circuit init(String[] args) {
        // at least 1 input.
        if (inputlen<1) return error("Expecting at least 1 input.");
        if (outputlen%4!=0) return error("Number of outputs should be a multiple of 4. Found " + outputlen);

        digits = outputlen/4;

        return this;
    }
}

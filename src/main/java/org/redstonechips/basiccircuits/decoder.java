package org.redstonechips.basiccircuits;

import java.util.Arrays;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class decoder extends Circuit {
    boolean[] register;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0 && state) {
            int i = (int)BooleanArrays.toUnsignedInt(inputs, 1, inputlen-1);
            Arrays.fill(register, false);
            register[i] = true;
            this.writeBits(register);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen<2) return error("Expecting at least 2 inputs.");
        if (outputlen>Math.pow(2, inputlen-1))
            return error("Bad number of outputs. Expecting up to " + (int)Math.pow(2, inputlen-1) + " outputs for " + inputlen + " inputs.");

        register = new boolean[outputlen];

        return this;
    }
}

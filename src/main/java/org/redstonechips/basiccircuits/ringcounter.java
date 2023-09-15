
package org.redstonechips.basiccircuits;

import java.util.BitSet;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class ringcounter extends counter {
    BitSet register;

    @Override
    public void input(boolean state, int inIdx) {
        if (state) {
            if (inIdx==0) { // clock pin
                register.clear();
                register.set(count);
                if (count<outputlen-1) count++;
                else count = 0;
            } else if (inIdx==1) { // reset pin
                register.clear();
                count = 0;
            }

            writeBitSet(register);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen==0) return error("Expecting at least 1 clock input. A 2nd reset input pin is optional.");
        if (outputlen==0) return error("Expecting at least 1 output.");

        register = new BitSet(outputlen);
        count = 0;

        return this;
    }
}

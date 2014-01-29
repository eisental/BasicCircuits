package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class not extends Circuit {

    @Override
    public void input(boolean state, int inIdx) {
        write(!state, inIdx);
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen!=outputlen) return error("Expecting the same number of inputs and outputs.");
        else return this;
    }
}

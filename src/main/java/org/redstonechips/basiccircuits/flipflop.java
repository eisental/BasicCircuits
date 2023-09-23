package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class flipflop extends Circuit {
    private boolean resetPinMode = false;

    @Override
    public void input(boolean state, int inIdx) {
        if (state) {
            if (resetPinMode) {
                if (inIdx == 0) { // reset
                    this.clearOutputs();
                } else {
                    this.write(!outputs[inIdx-1], inIdx-1);
                }
            } else this.write(!outputs[inIdx], inIdx);
        }

    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen!=inputlen && inputlen!=outputlen+1)
            return error("Expecting the same number of inputs and outputs or one extra input reset pin.");

        resetPinMode = (inputlen==outputlen+1);

        if (activator!=null) clearOutputs();
        return this;
    }

    @Override
    public boolean isStateless() {
        return false;
    }
}

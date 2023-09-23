
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class repeater extends Circuit {
    int outputSets;
    int outputSetSize;

    @Override
    public void input(boolean state, int inIdx) {
        if (outputSets==0)
            for (int i=0; i<outputlen; i++) write(state, i);
        else
            for (int j=0; j<outputSets; j++)
                write(state, j*outputSetSize+inIdx);
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen<1) return error("Expecting at least 1 input pin.");
        if (outputlen<1) return error("Expecting at least 1 output pin.");

        if (inputlen == 1)
            outputSets = 0; //optimize for original function
        else
            outputSets = outputlen/inputlen;

        if (outputSets != 0) {
            if (outputlen != inputlen*outputSets)
                return error("Tried to split "+outputlen+" into "+outputSets+", expected "+(inputlen*outputSets)+" outputs.");

            outputSetSize = outputlen/outputSets;
            if (outputSets==1)
                info("Repeating "+outputlen+" bits.");
            else
                info("Splitting "+inputlen+" bits into "+outputlen+", "+outputSets+" sets");
        } else {
            outputSetSize = 0; //not used
            if (outputlen==1)
                info("Repeating 1 bit.");
            else
                info("Splitting 1 bit into "+outputlen+".");
        }

        return this;
    }
}

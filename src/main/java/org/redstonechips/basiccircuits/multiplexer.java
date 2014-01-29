package org.redstonechips.basiccircuits;


import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class multiplexer extends Circuit {
    int selectSize, bitCount;
    int selection = 0;
    boolean[] select;
    boolean[][] inputBitSets;

    @Override
    public Circuit init(String[] args) {
        if (args.length==0) return error("Syntax for multiplexer is 'multiplexer <no. of input sets>.");

        try {
            int incount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(incount)/Math.log(2));
            int expectedInputs = incount*outputlen + selectSize;

            if (inputlen!=expectedInputs)
                return error("Wrong number of inputs (" + inputlen + "). expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");

            select = new boolean[selectSize];
            inputBitSets = new boolean[incount][outputlen];
            bitCount = outputlen;

            return this;
        } catch (NumberFormatException ne) {
            return error("Not a number: " + args[0]);
        }
    }

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx<selectSize) { // need to send a new input
            select[inIdx] = state;
            int i = (int)BooleanArrays.toUnsignedInt(select, 0, selectSize);
            if (i<inputBitSets.length) {
                selection = i;
                if (chip.hasListeners()) debug("Selecting input " + i);
                this.writeBits(inputBitSets[selection]);
            }

        } else { // update one of the bitsets
            int idxInBitSet = (inIdx-selectSize) % bitCount;
            int bitSetIdx = ((inIdx-selectSize)-idxInBitSet)/bitCount;
            inputBitSets[bitSetIdx][idxInBitSet] = state;
            if (bitSetIdx==selection) {
                this.writeBits(inputBitSets[selection]);
            }
        }
    }
}

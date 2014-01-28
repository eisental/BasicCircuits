package org.redstonechips.basiccircuits;


import java.util.Arrays;
import org.redstonechips.chip.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class demultiplexer extends Circuit {
    private int selectSize, bitCount, outcount, selection = -1;
    private boolean[] select;
    private boolean[] inputBitSet;
    private boolean[] output;


    @Override
    public Circuit init(String[] args) {
        if (args.length==0) return error("Syntax for multiplexer is 'multiplexer <no. of output sets>.");

        try {
            outcount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(outcount)/Math.log(2));
            bitCount = outputlen/outcount;
            int expectedInputs = bitCount + selectSize;

            if (inputlen!=expectedInputs) 
                return error("Wrong number of inputs. expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");

            output = new boolean[outputlen];
            select = new boolean[selectSize];
            inputBitSet = new boolean[bitCount];
            
            return this;
        } catch (NumberFormatException ne) {
            return error("Bad argument: " + args[0] + " expecting a number.");
        }
    }

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx<selectSize) { // selection change
            select[inIdx] = state;
            selection = BooleanArrays.toUnsignedInt(select);

            // clear the outputs
            Arrays.fill(output, false);

            if (chip.hasListeners()) debug("Selecting output " + selection);

        } else { // update in the input bit set
            inputBitSet[inIdx - selectSize] = state;
        }

        if (selection>=0 && selection<outcount) {
            // update selected output set.
            System.arraycopy(inputBitSet, 0, output, selection*bitCount, bitCount);
            this.writeBits(output);
        }
    }
}

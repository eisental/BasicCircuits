package org.tal.basiccircuits;


import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class demultiplexer extends Circuit {
    private int selectSize, bitCount, outcount, selection = -1;
    private BitSet7 select;
    private BitSet7 inputBitSet;
    private BitSet7 output;


    @Override
    public boolean init(Player player, String[] args) {
        if (args.length==0) {
            error(player, "Syntax for multiplexer is 'multiplexer <no. of output sets>.");
            return false;
        }

        try {
            outcount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(outcount)/Math.log(2));
            bitCount = outputs.length/outcount;
            int expectedInputs = bitCount + selectSize;

            if (inputs.length!=expectedInputs) {
                error(player, "Wrong number of inputs. expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");
                return false;
            }

            output = new BitSet7(outputs.length);
            select = new BitSet7(selectSize);
            inputBitSet = new BitSet7(bitCount);
            
            return true;
        } catch (NumberFormatException ne) {
            error(player, "Bad argument: " + args[0] + " expecting a number.");
            return false;
        }
    }

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx<selectSize) { // selection change
            select.set(inIdx, newLevel);
            selection = BitSetUtils.bitSetToUnsignedInt(select, 0, selectSize);

            // clear the outputs
            output.clear();

            if (hasDebuggers()) debug("Selecting output " + selection);

        } else { // update in the input bit set
            inputBitSet.set(inIdx - selectSize, newLevel);
        }

        if (selection>=0 && selection<outcount) {
            // update selected output set
            for (int i=0; i<bitCount; i++) {
                output.set(selection*bitCount+i, inputBitSet.get(i));
            }
            this.sendBitSet(output);
        }
    }
}

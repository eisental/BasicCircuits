package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class multiplexer extends Circuit {
    int selectSize, bitCount;
    int selection = 0;
    BitSet7 select;
    BitSet7[] inputBitSets;

    @Override
    public boolean init(CommandSender sender, String[] args) {
        if (args.length==0) {
            error(sender, "Syntax for multiplexer is 'multiplexer <no. of input sets>.");
            return false;
        }

        try {
            int incount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(incount)/Math.log(2));
            int expectedInputs = incount*outputs.length + selectSize;

            if (inputs.length!=expectedInputs) {
                error(sender, "Wrong number of inputs. expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");
                return false;
            }

            select = new BitSet7(selectSize);
            inputBitSets = new BitSet7[incount];
            for (int i=0; i<incount; i++) {
                inputBitSets[i] = new BitSet7(outputs.length);
                inputBitSets[i].clear();
            }

            bitCount = outputs.length;

            return true;
        } catch (NumberFormatException ne) {
            error(sender, "Not a number: " + args[0]);
            return false;
        }
    }

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx<selectSize) { // need to send a new input
            select.set(inIdx, newLevel);
            int i = BitSetUtils.bitSetToUnsignedInt(select, 0, selectSize);
            if (i<inputBitSets.length) {
                selection = i;
                if (hasDebuggers()) debug("Selecting input " + i);
                this.sendBitSet(inputBitSets[selection]);
            }

        } else { // update one of the bitsets
            int idxInBitSet = (inIdx-selectSize) % bitCount;
            int bitSetIdx = ((inIdx-selectSize)-idxInBitSet)/bitCount;
            inputBitSets[bitSetIdx].set(idxInBitSet, newLevel);
            if (bitSetIdx==selection) {
                this.sendBitSet(inputBitSets[selection]);
            }
        }
    }
}

package org.tal.basiccircuits;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class multiplexer extends Circuit {
    int selectSize, bitCount;
    int selection = -1;
    BitSet select;
    BitSet[] inputBitSets;

    @Override
    public boolean init(Player player, String[] args) {
        if (args.length==0) {
            error(player, "Syntax for multiplexer is 'multiplexer <no. of input sets>.");
            return false;
        }

        try {
            int incount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(incount)/Math.log(2));
            int expectedInputs = incount*outputs.length + selectSize;

            if (inputs.length!=expectedInputs) {
                error(player, "Wrong number of inputs. expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");
                return false;
            }

            select = new BitSet(selectSize);
            inputBitSets = new BitSet[incount];
            for (int i=0; i<incount; i++) {
                inputBitSets[i] = new BitSet(outputs.length);
                inputBitSets[i].clear();
            }

            bitCount = outputs.length;

            return true;
        } catch (NumberFormatException ne) {
            error(player, "Not a number: " + args[0]);
            return false;
        }
    }

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx<selectSize) { // need to send a new input
            select.set(inIdx, newLevel);
            int i = Circuit.bitSetToUnsignedInt(select, 0, selectSize);
            if (i<inputBitSets.length) {
                selection = i;                
                this.sendBitSet(inputBitSets[selection]);
            }

        } else { // update one of the bitsets
            int idxInBitSet = (inIdx-selectSize) % bitCount;
            int bitSetIdx = ((inIdx-selectSize)-idxInBitSet)/bitCount;
            inputBitSets[bitSetIdx].set(idxInBitSet, newLevel);
            if (bitSetIdx==selection)
                this.sendBitSet(inputBitSets[selection]);
        }
    }

    @Override
    public void loadState(Map<String, String> state) {
        inputBits = Circuit.loadBitSet(state, "inputBits", inputs.length);

        int curBit = 0;
        for (int i=0; i<selectSize; i++) {
            select.set(i, inputBits.get(i));
        }

        for (BitSet s : this.inputBitSets) {
            for (int i=0; i<bitCount; i++) {
                s.set(i, inputBits.get(curBit+i+selectSize));
            }
            curBit += bitCount;
        }

        int i = Circuit.bitSetToUnsignedInt(select, 0, selectSize);
        if (i<inputBitSets.length) {
            selection = i;
            this.sendBitSet(inputBitSets[selection]);
        }

    }

    @Override
    public Map<String, String> saveState() {
        return Circuit.storeBitSet(new HashMap<String,String>(), "inputBits", inputBits, inputs.length);
    }

}

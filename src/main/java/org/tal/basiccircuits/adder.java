package org.tal.basiccircuits;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.BitSetCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class adder extends BitSetCircuit {
    BitSet7 constant;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet7 set) {
        BitSet7 output = (BitSet7)constant.clone();
        for (BitSet7 s : this.inputBitSets) {
            output = addBitSets(output, s, outputs.length);
        }

        this.sendBitSet(output);
    }

    public static BitSet7 addBitSets(BitSet7 aSet, BitSet7 bSet, int length) {
        BitSet7 s = (BitSet7)aSet.clone();
        s.xor(bSet);
        BitSet7 c = (BitSet7)aSet.clone();
        c.and(bSet);

        while (!c.isEmpty()) {
            BitSetUtils.shiftLeft(c, length);
            BitSet7 oldS = (BitSet7)s.clone();
            s.xor(c);
            c.and(oldS);
        }

        s.clear(length, s.size());

        return s;
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (outputs.length==0) {
            error(player, "Expecting at least 1 output pin.");
            return false;
        }

        if (args.length==0) {
            error(player, "Wordlength sign argument is missing.");
            return false;
        }

        try {
            wordlength = Integer.decode(args[0]);
        } catch (NumberFormatException ne) {
            error(player, "Bad wordlength sign argument: " + args[0] + " expecting a number.");
            return false;
        }

        if ((inputs.length % wordlength)==0) {
            int inBitSetCount = inputs.length / wordlength;
            info(player, "Activating adder with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new BitSet7[inBitSetCount];
            for (int i=0; i<inBitSetCount; i++) {
                inputBitSets[i] = new BitSet7(wordlength);
                inputBitSets[i].clear();
            }
        } else {
            error(player, "Invalid number of inputs (" + inputs.length + "). Number of inputs must be a multiple of the word length.");
            return false;
        }

        int iconstant = 0;
        if (args.length>1) {
            try {
                iconstant = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                error(player, "Bad constant argument: " + args[1] + " expecting a number.");
                return false;
            }
        }

        constant = BitSetUtils.intToBitSet(iconstant, wordlength); // TODO: support two's complement
        
        int maxResult = ((int)Math.pow(2, wordlength)-1) * inputBitSets.length + iconstant;
        int expectedOutputs = (int)Math.ceil(Math.log(maxResult)/Math.log(2));

        if (outputs.length<expectedOutputs) {
            error(player, ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. Circuit should have " + expectedOutputs + " output bits.");
        }
        return true;
    }
}

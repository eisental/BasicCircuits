package org.tal.basiccircuits;


import org.bukkit.entity.Player;
import org.tal.redstonechips.BitSetCircuit;
import org.tal.redstonechips.Circuit;
import org.tal.redstonechips.util.BitSet7;

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
            output = addBitSets(output, s, wordlength);
        }

        this.sendBitSet(output);
    }

    public static BitSet7 addBitSets(BitSet7 aSet, BitSet7 bSet, int length) {
        BitSet7 s = (BitSet7)aSet.clone();
        s.xor(bSet);
        BitSet7 c = (BitSet7)aSet.clone();
        c.and(bSet);

        while (!c.isEmpty()) {
            shiftLeft(c, length);
            BitSet7 oldS = (BitSet7)s.clone();
            s.xor(c);
            c.and(oldS);
        }

        s.clear(length, s.size());

        return s;
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (!super.init(player, args)) return false;
        if (args.length>0) {
            try {
                int c = Integer.decode(args[0]);
                constant = Circuit.intToBitSet(c, wordlength); // TODO: support two's complement
                return true;
            } catch (NumberFormatException ne) {
                error(player, "Bad argument: " + args[0] + " expected a number.");
                return false;
            }
        }

        return true;
    }
}

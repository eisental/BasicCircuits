package org.tal.basiccircuits;


import java.util.BitSet;
import org.bukkit.entity.Player;
import org.tal.redstonechips.BitSetCircuit;
import org.tal.redstonechips.Circuit;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class multiplier extends BitSetCircuit {
    int constant = 0;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet set) {
        int mul = constant;
        for (BitSet s : this.inputBitSets) {
            mul = mul * Circuit.bitSetToUnsignedInt(s, 0, wordlength);
        }

        this.sendInt(0, outputs.length, mul);
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (!super.init(player, args)) return false;
        if (args.length>0) {
            try {
                constant = Integer.decode(args[0]);
                return true;
            } catch (NumberFormatException ne) {
                player.sendMessage("Bad argument: " + args[0] + " expected a number.");
                return false;
            }
        } else constant = 1;

        return true;
    }
}

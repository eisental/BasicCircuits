package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class decoder extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==0 && newLevel) {
            int i = Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
            outputBits.clear();
            outputBits.set(i);
            this.sendBitSet(0, outputs.length, outputBits);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length<2) {
            if (player!=null) player.sendMessage("Expecting at least 2 inputs.");
            return false;
        }
        if (outputs.length!=Math.pow(2, inputs.length-1)) {
            if (player!=null) player.sendMessage("Bad number of outputs. Expecting " + Math.pow(2, inputs.length-1));
            return false;
        }

        return true;
    }

}

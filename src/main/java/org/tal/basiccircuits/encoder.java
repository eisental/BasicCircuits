package org.tal.basiccircuits;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class encoder extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inputBits.isEmpty()) sendBitSet(inputBits);
        else {
            sendInt(0, outputs.length, inputBits.length()-1);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=Math.pow(2, outputs.length)) {
            error(player, "Number of inputs must be 2 to the power of number of outputs.");
            return false;
        }

        return true;
    }

}

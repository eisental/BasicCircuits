package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class flipflop extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (newLevel) {
            this.sendOutput(inIdx, !outputBits.get(inIdx));
        }

    }

    @Override
    public boolean init(Player player, String[] args) {
        if (outputs.length!=inputs.length) {
            player.sendMessage("flipflop number of outputs must match number of inputs.");
            return false;
        } else return true;
    }

}

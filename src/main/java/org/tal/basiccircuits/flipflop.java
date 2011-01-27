package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

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
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        } else return true;
    }

}

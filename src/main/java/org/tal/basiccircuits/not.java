/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class not extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        sendOutput(inIdx, !newLevel);
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        }

        return true;
    }

}

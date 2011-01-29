/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class terminal extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) { }

    @Override
    protected boolean init(Player player, String[] args) {
        if (outputs.length!=9) {
            error(player, "Expecting 9 outputs. 1 clock output and 8 data outputs.");
            return false;
        }

        return true;
    }
}


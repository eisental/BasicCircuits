/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class repeater extends Circuit {

    @Override
    public void inputChange(int idx, boolean state) {
        if (idx==0) for (int i=0; i<outputs.length; i++) sendOutput(i, state);
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        return true;
    }

}

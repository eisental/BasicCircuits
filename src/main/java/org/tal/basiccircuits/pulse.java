/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;
import org.tal.redstonechips.util.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class pulse extends Circuit {
    long interval;

    @Override
    public void inputChange(final int inIdx, boolean high) {
        if (high) {
            new Thread() {
                @Override
                public void run() {
                    sendOutput(inIdx, true);
                    try {
                        sleep(interval);
                    } catch (InterruptedException ex) {}
                    sendOutput(inIdx, false);
                }
            }.start();
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        }

        if (inputs.length==0) {
            error(player, "Expecting at least one input and one output.");
        }

        if (args.length==0) interval = 1000; // 1 sec default
        else interval = Math.round(UnitParser.parse(args[0]));

        return true;
    }

}

package org.tal.basiccircuits;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class counter extends Circuit {
    private static final int inputPin = 0;
    private static final int resetPin = 1;

    int count = 0;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==inputPin) {
            if (newLevel) { // high from low
                count++;
                this.sendInt(0, outputs.length, count);
            }
        } else if (inIdx==resetPin) {
            if (newLevel) { // high from low
                count = 0;
                this.sendInt(0, outputs.length, count);
            }
        }

    }

    @Override
    public boolean init(Player player, String[] args) {
        if (inputs.length==0) {
            error(player, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            try {
                count = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(player, "Invalid count init argument: " + args[0]);
                return false;
            }
        }

        return true;
    }
}

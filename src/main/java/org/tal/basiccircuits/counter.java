package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class counter extends Circuit {
    private static final int inputPin = 0;
    private static final int resetPin = 1;

    int min;
    int max;
    int direction;
    int count;
    boolean updown = false;

    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inIdx==inputPin) {
            if (on) { // high from low
                if (direction==1 && count>=max) {
                    if (updown) {
                        direction = -1;
                        count = max-1;
                    } else count = min;
                } else if (direction==-1 && count<=min) {
                    if (updown) {
                        direction = 1;
                        count = min+1;
                    } else count = max;
                } else count+=direction;

                if (hasDebuggers()) debug("set to " + count);
                this.sendInt(0, outputs.length, count);
            }
        } else if (inIdx==resetPin) {
            if (on) { // high from low
                if (direction==-1 && !updown )
                    count = max;
                else count = min;
                if (hasDebuggers()) debug("Count reset to " + count);
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

        if (args.length==0) {
            min = 0;
            max = (int)Math.pow(2, outputs.length);
            direction = 1;
        } else if (args.length==1) {
            // just max
            try {
                max = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(player, "Bad max argument: " + args[0]);
                return false;
            }

            min = 0;
            direction = 1;
        }  else if (args.length==2) {
            // min and max
            try {
                min = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(player, "Bad min argument: " + args[0]);
                return false;
            }

            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                error(player, "Bad max argument: " + args[1]);
                return false;
            }

            if (min>max) {
                direction = -1;
                int t = max;
                max = min;
                min = t;
            } else direction = 1;
        } else if (args.length==3) {
            // min max and direction
            try {
                min = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(player, "Bad min argument: " + args[0]);
                return false;
            }
            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                error(player, "Bad max argument: " + args[1]);
                return false;
            }

            try {
                if (args[2].equalsIgnoreCase("up"))
                    direction = 1;
                else if (args[2].equalsIgnoreCase("down"))
                    direction = -1;
                else if (args[2].equalsIgnoreCase("updown")) {
                    direction = 1;
                    updown = true;
                }
            } catch (NumberFormatException ne) {
                error(player, "Bad direction argument: " + args[2]);
                return false;
            }
        }

        if (direction == 1) count = min;
        else count = max;

        return true;
    }
}

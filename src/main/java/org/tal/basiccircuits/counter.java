package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class counter extends Circuit {
    private static final int incPin = 0;
    private static final int resetPin = 1;
    private static final int directionPin = 2;

    int min;
    int max;
    int direction;
    int count;
    int reset;
    boolean updown = false;

    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inIdx==incPin && on) {
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

            if (hasDebuggers()) debug("Counting " + count + ".");
            this.sendInt(0, outputs.length, count);
        } else if (inIdx==resetPin && on) {
            count = reset;
            if (hasDebuggers()) debug("Resetting counter to " + count + ".");
            this.sendInt(0, outputs.length, count);
        } else if (inIdx==directionPin) {
            direction = (on?1:-1);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }

        if (args.length==0) {
            min = 0;
            max = (int)Math.pow(2, outputs.length) - 1;
            direction = 1;
        } else if (args.length==1) {
            // just max
            try {
                max = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(sender, "Bad max argument: " + args[0]);
                return false;
            }

            min = 0;
            direction = 1;
        }  else if (args.length==2) {
            // min and max
            try {
                min = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                error(sender, "Bad min argument: " + args[0]);
                return false;
            }

            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                error(sender, "Bad max argument: " + args[1]);
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
                error(sender, "Bad min argument: " + args[0]);
                return false;
            }
            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                error(sender, "Bad max argument: " + args[1]);
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
                error(sender, "Bad direction argument: " + args[2]);
                return false;
            }
        }

        if (inputs.length==3) { // has a direction pin.
            direction = (inputBits.get(2)?1:-1);
        }

        if (direction == 1) count = reset = min;
        else count = reset = max;

        resetOutputs();
        return true;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        String loadedCount = state.get("count");
        if (loadedCount!=null) {
            count = Integer.decode(loadedCount);
            sendInt(count, 0, outputs.length);
        }

        String loadedDirection = state.get("direction");
        if (loadedDirection!=null) {
            direction = Integer.decode((String)loadedDirection);
        }
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String, String>();
        state.put("count", Integer.toString(count));
        state.put("direction", Integer.toString(direction));
        return state;
    }
}

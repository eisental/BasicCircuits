package org.redstonechips.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.redstonechips.circuit.Circuit;

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

    boolean updown = false;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==incPin && state) {
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

            if (chip.hasListeners()) debug("Counting " + count + ".");
            this.writeInt(count, 0, outputlen);
        } else if (inIdx==resetPin && state) {
            count = (direction==1?min:max);
            if (chip.hasListeners()) debug("Resetting counter to " + count + ".");
            this.writeInt(count, 0, outputlen);
        } else if (inIdx==directionPin) {
            direction = (state?1:-1);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen==0) return error("Expecting at least 1 input.");

        if (args.length==0) {
            min = 0;
            max = (int)Math.pow(2, outputlen) - 1;
            direction = 1;
        } else if (args.length==1) {
            // just max
            try {
                max = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                return error("Bad max argument: " + args[0]);
            }

            min = 0;
            direction = 1;
        }  else if (args.length==2) {
            // min and max
            try {
                min = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                return error("Bad min argument: " + args[0]);
            }

            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                return error("Bad max argument: " + args[1]);
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
                return error("Bad min argument: " + args[0]);
            }
            try {
                max = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                return error("Bad max argument: " + args[1]);
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
                return error("Bad direction argument: " + args[2]);
            }
        }

        if (direction == 1) count = min;
        else count = max;

        return this;
    }

    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        String loadedCount = state.get("count");
        if (loadedCount!=null) {
            count = Integer.decode(loadedCount);
            writeInt(count, 0, outputlen);
        }

        String loadedDirection = state.get("direction");
        if (loadedDirection!=null) {
            direction = Integer.decode(loadedDirection);
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

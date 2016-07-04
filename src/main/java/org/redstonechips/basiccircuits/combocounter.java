package org.redstonechips.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.redstonechips.circuit.Circuit;

/**
 * Combination counter that can both increment and decrement
 */
public class combocounter extends Circuit {
    private int incPin = 0;
    private int decPin = 1;
    private int resetPin = 2;

    int min;
    int max;
    int count;

    @Override
    public void input(boolean state, int inIdx) {
        if (state) {
            if (inIdx == incPin) {
                if (count >= max) {
                    count = min;
                }
                else {
                    count = count + 1;
                }
                
                if (chip.hasListeners()) debug("Counting " + count + ".");
                this.writeInt(count, 0, outputlen);
            }
            else if (inIdx == decPin) {
                if (count <= min) {
                    count = max;
                }
                else {
                    count = count - 1;
                }
                
                if (chip.hasListeners()) debug("Counting " + count + ".");
                this.writeInt(count, 0, outputlen);
            }
            else if (inIdx == resetPin) {
                count = 0;
                
                if (chip.hasListeners()) debug("Resetting counter to " + count + ".");
                this.writeInt(count, 0, outputlen);
            }
            
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen == 0) {
            return error("Expecting at least 1 input.");
        }

        if (args.length==0) {
            min = 0;
            max = (int)Math.pow(2, outputlen) - 1;
        }
        else {
            return error("Illegal number of arguments (max 0).");
        }
        
        count = min;
        return this;
    }

    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        String loadedCount = state.get("count");
        if (loadedCount != null) {
            count = Integer.decode(loadedCount);
            writeInt(count, 0, outputlen);
        }
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String, String>();
        state.put("count", Integer.toString(count));
        return state;
    }
}

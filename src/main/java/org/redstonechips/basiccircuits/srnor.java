package org.redstonechips.basiccircuits;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.redstonechips.Serializer;
import org.redstonechips.chip.Circuit;

/**
 *
 * @author Matthew Peychich
 */
public class srnor extends Circuit {
    boolean[] register;

    @Override
    public void input(boolean state, int inIdx) {
      // only update outputs when an input goes low to high.
      if (state) {
          Arrays.fill(register, true); // turn every bit on
          register[inIdx] = false; // turn respective output bit off
          this.writeBits(register);
      }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen!=outputlen) return error("Expecting the same number of inputs and outputs.");
        if (activator!=null) clearOutputs();
        
        register = new boolean[outputlen];
        
        return this;
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String, String> state = new HashMap<>();
        return Serializer.booleanArrayToMap(state, "register", register);
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        if (state.containsKey("register")) {
            register = Serializer.mapToBooleanArray(state, "register");
            this.writeBits(register);
        }
    }

    @Override
    public boolean isStateless() {
        return false;
    }
}

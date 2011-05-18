package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Matthew Peychich
 */
public class srnor extends Circuit {
    BitSet7 register = new BitSet7();

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
      // only update outputs when an input goes low to high.
      if (newLevel) {
          register.set(0, inputs.length); // turn every bit on
          register.set(inIdx, false); // turn respective output bit off
          this.sendBitSet(register);
      }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=outputs.length) {
            error(sender, "Expecting the same number of inputs and outputs.");
            return false;
        }

        if (sender!=null) resetOutputs();;
        return true;
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String, String> state = new HashMap<String,String>();
        BitSetUtils.bitSetToMap(state, "register", register, outputs.length);
        return state;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        if (state.containsKey("register")) {
            register = BitSetUtils.mapToBitSet(state, "register");
            this.sendBitSet(register);
        }
    }

    @Override
    protected boolean isStateless() {
        return false;
    }

}

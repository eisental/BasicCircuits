package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Matthew Peychich
 */
public class srnor extends Circuit {
    BitSet7 register;

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
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        }

        register = new BitSet7(outputs.length);
        return true;
    }

}

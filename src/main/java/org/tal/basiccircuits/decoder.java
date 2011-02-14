package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class decoder extends Circuit {
    BitSet7 register;

    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inIdx==0 && on) {
            int i = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
            register.clear();
            register.set(i);
            this.sendBitSet(0, outputs.length, register);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length<2) {
            error(player, "Expecting at least 2 inputs.");
            return false;
        }
        if (outputs.length!=Math.pow(2, inputs.length-1)) {
            error(player, "Bad number of outputs. Expecting " + (int)Math.pow(2, inputs.length-1));
            return false;
        }

        register = new BitSet7(outputs.length);
        return true;
    }

}

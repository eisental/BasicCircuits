package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class flipflop extends Circuit {
    private boolean resetPinMode = false;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (newLevel) {
            if (resetPinMode) {
                if (inIdx == 0) { // reset
                    this.sendBitSet(BitSetUtils.clearBitSet);
                } else {
                    this.sendOutput(inIdx-1, !outputBits.get(inIdx-1));
                }
            } else this.sendOutput(inIdx, !outputBits.get(inIdx));
        }

    }

    @Override
    public boolean init(CommandSender sender, String[] args) {
        if (outputs.length!=inputs.length && inputs.length!=outputs.length+1) {
            error(sender, "Expecting the same number of inputs and outputs or one extra input reset pin.");
            return false;
        }

        resetPinMode = (inputs.length==outputs.length+1);

        if (sender!=null) resetOutputs();
        return true;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }
}

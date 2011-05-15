
package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class burst extends Circuit {
    int pulseCount;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==0 && state) {
            pulse(pulseCount);
        } else if (inIdx>0) {
            pulseCount = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
        }
    }

    private void pulse(int count) {
        if (hasDebuggers()) debug("Pulsing " + count + " time(s).");
        for (int i=0; i<count; i++) {
            for (int m=0; m<outputs.length; m++) {
                sendOutput(m, true);
                sendOutput(m, false);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length>0) {
            try {
                pulseCount = Integer.decode(args[0]);
                if (pulseCount<0) {
                    error(sender, "Bad pulse count argument: " + args[0]);
                    return false;
                }
            } catch (NumberFormatException ne) {
                error(sender, "Bad pulse count argument: " + args[0]);
                return false;
            }
        }

        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }

        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input pin.");
            return false;
        }

        return true;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }
}

package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Domenico Foti
 */
public class shifter extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0)) { //First Input is Clock
            if (outputBits.isEmpty())
                shift(inputBits.get(2), inputBits.get(3, inputs.length));
            else
                shift(inputBits.get(2), outputBits);

            sendBitSet(outputBits);
        }

        if (inputBits.get(1)) {
            sendBitSet(BitSetUtils.clearBitSet);
        }
    }

    public void shift(boolean direction, BitSet7 in) {
	for(int i=0; i<outputs.length; i++) {
            if (!direction) {
                outputBits.set(i, in.get(i+1));
            } else {
                outputBits.set(i, in.get(i-1));
            }
	}
}

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length != inputs.length - 3) {
            error(sender, "This chip requires three less outputs than inputs.");
            return false;
        }
        if (inputs.length < 5) {
            error(sender, "This chip requires atleast 5 inputs. 1 clock pin. 1 reset pin. 1 shift pin. 2 or more data pins.");
            return false;
        }

        return true;
    }
}

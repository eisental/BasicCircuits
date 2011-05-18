package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Domenico Foti
 */
public class barrelshifter extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0)) {	//First Input is Clock
            if (outputBits.length() == 0) {
                barrel(inputBits.get(2), inputBits.get(3, inputs.length));
            } else {
                barrel(inputBits.get(2), outputBits);
            }

            sendBitSet(outputBits);
        }

	if (inputBits.get(1)) { // Reset pin.
            sendBitSet(BitSetUtils.clearBitSet);
	}
    }

    public void barrel(boolean direction, BitSet7 in) { // Left is false or 0 and Right is true or 1
 	if (!direction) {
            for (int i = 0; i < outputs.length; i++) {
                if (i + 1 < outputs.length) {
                    outputBits.set(i + 1, in.get(i));
                } else {
                    outputBits.set(0, in.get(outputs.length-1));
                    outputBits.set(outputs.length-1, in.get(outputs.length-2));
                }
            }
        } else {
            for (int i=0; i < outputs.length; i++) {
                if (i < outputs.length)
                    outputBits.set(i, in.get(i+1));
                else {
                    outputBits.set(outputs.length-2, in.get(outputs.length-1));
                    outputBits.set(outputs.length-1, in.get(0));
                }
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length != inputs.length - 3) {
            error(sender, "This chip requires three less output than data inputs.");
            return false;
        }
        if (inputs.length <= 3) {
            error(sender, "This chip requires atleast 5 inputs. 1 clock pin. 1 reset pin. 1 direction pin. 2 or more data pins.");
            return false;
        }

        return true;
    }
}

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
                barrel(inputBits.get(2), inputs);
            } else {
                barrel(inputBits.get(2), outputBits);
            }

            sendBitSet(outputBits);
        }

	if (inputBits.get(1)) { // Reset pin.
            sendBitSet(BitSetUtils.clearBitSet);
	}
    }

    public void barrel(boolean direction, int[] in) { // Left is false or 0 and Right is true or 1
 	if (!direction) {
                  for(int i = 1; i<outputs.length-1; i++) {
                    output[i] = in[i+2];
        } 
                    outputs[0] = in[in.length];
 	if (!direction) {
                  for(int i = outputs.length-1; i>3; i--) {
                    output[i] = in[i-1];
        } 
                    outputs[outputs.length] = in[3];
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

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

    BitSet7 output = null;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0)) { //First Input is Clock
            if (!inputBits.get(1)) {	//Second Input is Direction
                output = BitSetUtils.shiftLeft(inputBits.get(1, inputs.length), inputs.length - 1);
            } else {
                output = BitSetUtils.shiftRight(inputBits.get(1, inputs.length), inputs.length - 1, false);
            }
            this.sendBitSet(output);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length != inputs.length - 2) {
            error(sender, "This chip requires two less outputs than inputs.");
            return false;
        }
        if (inputs.length <= 3) {
            error(sender, "This chip requires atleast 4 inputs. 1 clock pin. 1 shift pin. 2 or more data pins.");
            return false;
        }

        return true;
    }
}

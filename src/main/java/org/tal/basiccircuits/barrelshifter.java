package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Domenico Foti
 */
public class barrelshifter extends Circuit {

    BitSet7 output = null;
    int r;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0) == true) {	//First Input is Clock
            barrel(inputBits.get(1));
            this.sendBitSet(output);
        }
    }

    public void barrel(boolean direction) { // Left is false or 0 and Right is true or 1
        if (!direction) {
            for (int i = 0; i <= r; i++) {
                if (i + 1 < r) {
                    output.set(i + 1, inputBits.get(i));
                }
                if (i < r && !(i + 1 < r)) { //As i approaches r, input 1 must be swapped to r before swapping r
                    output.set(2, inputBits.get(r));
                    output.set(r, inputBits.get(r - 1));
                }
            }
        } else {
            for (int i = 0; i <= r; i++) {
                if (i - 1 < r) {
                    output.set(i, inputBits.get(i + 1));
                }
                if (i == r - 1 && !(i - 1 < r)) { //As i approaches r, r must be swapped to input 1 before swapping r
                    output.set(r,inputBits.get(2));
                    output.set(r-1, inputBits.get(r));
                }
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length != inputs.length - 2) {
            error(sender, "This chip requires two less output than data inputs.");
            return false;
        }
        if (inputs.length <= 3) {
            error(sender, "This chip requires atleast 4 inputs. 1 clock pin. 1 shift pin. 2 or more data pins.");
            return false;
        }

        r = inputs.length - 2;

        return true;
    }
}

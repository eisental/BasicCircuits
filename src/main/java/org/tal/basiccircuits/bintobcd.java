package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class bintobcd extends Circuit {
    int digits;

    @Override
    public void inputChange(int inIdx, boolean state) {
        String value;
        
        value = Integer.toString(BitSetUtils.bitSetToUnsignedInt(inputBits, 0, inputs.length));

        for (int i=0; i<digits; i++) {
            String d;
            int digit;

            if (i<value.length()) {
                d = Character.toString(value.charAt(value.length()-i-1));
                digit = Integer.decode(d);
            } else
                digit = 0;
            
            this.sendInt(i*4, 4, digit);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        // at least 1 input.
        if (inputs.length<1) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }

        if (outputs.length%4!=0) {
            error(sender, "Number of outputs should be a multiple of 4. Found " + outputs.length);
            return false;
        }

        digits = outputs.length/4;

        return true;
    }

}

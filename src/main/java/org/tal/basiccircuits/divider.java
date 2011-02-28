package org.tal.basiccircuits;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.BitSetCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class divider extends BitSetCircuit {
    int constant = 1;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet7 set) {
        int div = BitSetUtils.bitSetToUnsignedInt(inputBitSets[0], 0, wordlength);
        if (inputBitSets.length>1) {
            for (int i=1; i<inputBitSets.length; i++) {
                int num = BitSetUtils.bitSetToUnsignedInt(inputBitSets[i], 0, wordlength);
                if (num==0) { div = 0; break; }
                div = Math.round(div/num);
            }
        }

        div = Math.round(div / constant);

        this.sendInt(0, outputs.length, div);
    }

    @Override
    public boolean init(CommandSender sender, String[] args) {
        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }

        if (args.length==0) {
            error(sender, "Wordlength sign argument is missing.");
            return false;
        }

        try {
            wordlength = Integer.decode(args[0]);
        } catch (NumberFormatException ne) {
            error(sender, "Bad wordlength sign argument: " + args[0] + " expecting a number.");
            return false;
        }

        if ((inputs.length % wordlength)==0) {
            int inBitSetCount = inputs.length / wordlength;
            info(sender, "Activating adder with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new BitSet7[inBitSetCount];
            for (int i=0; i<inBitSetCount; i++) {
                inputBitSets[i] = new BitSet7(wordlength);
                inputBitSets[i].clear();
            }
        } else {
            error(sender, "Invalid number of inputs (" + inputs.length + "). Number of inputs must be a multiple of the word length.");
            return false;
        }

        if (args.length>1) {
            try {
                constant = Integer.decode(args[1]);
                if (constant==0) {
                    error(sender, "Bad constant argument: " + args[0] + ". Division by zero is not allowed.");
                }
            } catch (NumberFormatException ne) {
                error(sender, "Bad constant argument: " + args[1] + " expecting a number.");
                return false;
            }
        }

        int expectedOutputs = wordlength;
        System.out.println("expectedOutputs=" + expectedOutputs);
        if (outputs.length<expectedOutputs) {
            error(sender, ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. To prevent this, the circuit should have " + expectedOutputs + " output bits.");
        }
        return true;    
    }
}

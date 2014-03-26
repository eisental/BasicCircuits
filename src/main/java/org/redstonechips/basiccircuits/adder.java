package org.redstonechips.basiccircuits;


import org.bukkit.ChatColor;
import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class adder extends BitSetCircuit {

    int constant = 0;

    boolean subtract = false;

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        boolean[] output = null;

        // add bitsets
        for (boolean[] s : this.inputBitSets) {
            if (output==null) output = s;
            else {
                if (subtract)
                    output = BooleanArrays.add(output, -BooleanArrays.toUnsignedInt(s), outputlen);
                else
                    output = BooleanArrays.add(output, s, outputlen);
            }
        }

        // add constant
        if (constant != 0) {
            if (subtract)
                output = BooleanArrays.add(output, -constant, outputlen);
            else output = BooleanArrays.add(output, constant, outputlen);
        }
        
        // write result
        this.writeBits(output);
    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen==0) return error("Expecting at least 1 output pin.");
        if (args.length==0) return error("wordlength sign argument is missing.");

        try {
            wordlength = Integer.decode(args[0]);
            if (wordlength<=0) return error("Bad wordlength argument: " + args[0] + ".\nExpecting a number greater than 0.");
        } catch (NumberFormatException ne) {
            return error("Bad wordlength argument: " + args[0] + ".\nExpecting a number greater than 0.");
        }

        if ((inputlen % wordlength)==0) {
            int inBitSetCount = inputlen / wordlength;
            info("Activating adder with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new boolean[inBitSetCount][wordlength];            
        } else return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the word length.");

        if (args.length>1) {
            try {
                constant = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                return error("Bad constant argument: " + args[1] + " expecting a number.");
            }
        } if (args.length>2) {
            if (args[2].equalsIgnoreCase("subtract")) {
                // subtract mode
                subtract = true;
            } else return error("Bad argument value: " + args[2]);
        }
        
        int maxResult = ((int)Math.pow(2, wordlength)-1) * inputBitSets.length + constant;
        int expectedOutputs = (int)Math.ceil(Math.log(maxResult)/Math.log(2));

        if (outputlen<expectedOutputs) 
            info(ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. Circuit should have " + expectedOutputs + " output bits.");
        
        return this;
    }
}

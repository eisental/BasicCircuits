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
            output = BooleanArrays.add(output, constant, outputlen);
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
            inputBitSets = new boolean[inBitSetCount][wordlength];
        } else return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the word length.");

        if (args[args.length-1].equalsIgnoreCase("subtract"))
            subtract = true;

        if ((args.length>1 && !subtract) || (args.length>2)) {
            try {
                constant = Integer.decode(args[1]);
                if (subtract) constant = -constant;
            } catch (NumberFormatException ne) {
                return error("Bad constant argument: " + args[1] + " expecting a number.");
            }
        }

        int maxResult = ((int)Math.pow(2, wordlength)-1) * inputBitSets.length + constant;
        int expectedOutputs = (int)Math.ceil(Math.log(maxResult)/Math.log(2));

        if (outputlen<expectedOutputs)
            info(ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. Circuit should have " + expectedOutputs + " output bits.");

        info("Activating adder with " + inputBitSets.length + " input set(s) of " + wordlength +
                " bits each. The chip is running in " + (subtract?"subtract":"add") + " mode" + (constant!=0? ", with a constant value of " + constant:"") + ".");
        return this;
    }
}

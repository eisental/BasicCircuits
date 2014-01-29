package org.redstonechips.basiccircuits;


import org.bukkit.ChatColor;
import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;



/**
 *
 * @author Tal Eisenberg
 */
public class multiplier extends BitSetCircuit {
    long constant = 1;

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        long mul = constant;
        for (boolean[] s : this.inputBitSets) {
            mul = mul * BooleanArrays.toUnsignedInt(s);
        }

        this.writeInt(mul, 0, outputlen);
    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen==0) return error("Expecting at least 1 output pin.");
        if (args.length==0) return error("Wordlength sign argument is missing.");

        try {
            wordlength = Integer.decode(args[0]);

            if (wordlength<=0) 
                return error("Bad wordlength sign argument: " + args[0] + ". Expecting a number greater than 0.");
        } catch (NumberFormatException ne) {
            return error("Bad wordlength sign argument: " + args[0] + ". Expecting a number greater than 0.");
        }

        if ((inputlen % wordlength)==0) {
            int inBitSetCount = inputlen / wordlength;
            info("Activating multiplier with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new boolean[inBitSetCount][wordlength];
        } else {
            return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the word length.");
        }

        if (args.length>1) {
            try {
                constant = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                return error("Bad constant argument: " + args[1] + " expecting a number.");
            }
        }

        long maxResult = (long)Math.pow(Math.pow(2, wordlength)-1, inputBitSets.length) * constant;

        int expectedOutputs = (int)Math.ceil(Math.log(maxResult)/Math.log(2));
        if (outputlen<expectedOutputs)
            info (ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. To prevent this, the circuit should have " + expectedOutputs + " output bits.");
        
        return this;
    }
}

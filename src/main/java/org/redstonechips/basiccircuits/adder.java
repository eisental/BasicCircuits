package org.redstonechips.basiccircuits;


import java.util.Arrays;
import org.bukkit.ChatColor;
import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class adder extends BitSetCircuit {

    boolean[] constant;

    boolean subtract = false;

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        boolean[] output = null;

        for (boolean[] s : this.inputBitSets) {
            if (output==null) output = s;
            else {
                if (subtract)
                    output = addBitSets(output, negate(s, outputlen), outputlen);
                else
                    output = addBitSets(output, s, outputlen);
            }
        }

        if (subtract)
            output = addBitSets(output, negate(constant, outputlen), outputlen);
        else output = addBitSets(output, constant, outputlen);

        this.writeBits(output);
    }

    public static boolean[] addBitSets(boolean[] a, boolean[] b, int length) {
        boolean[] s = new boolean[length];
        BooleanArrays.xor(s, a, b);
        boolean[] c = new boolean[length];
        BooleanArrays.and(c, a, b);

        while (!BooleanArrays.isZero(c)) {
            BooleanArrays.shiftLeft(c, c);
            boolean[] oldS = s.clone();
            BooleanArrays.xor(s, s, c);
            BooleanArrays.and(c, c, oldS);
        }
        
        Arrays.fill(s, length, s.length, false);

        return s;
    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen==0) return error("Expecting at least 1 output pin.");
        if (args.length==0) return error("Wordlength sign argument is missing.");

        try {
            wordlength = Integer.decode(args[0]);
            if (wordlength<=0) return error("Bad wordlength sign argument: " + args[0] + ".\nExpecting a number greater than 0.");
        } catch (NumberFormatException ne) {
            return error("Bad wordlength sign argument: " + args[0] + ".\nExpecting a number greater than 0.");
        }

        if ((inputlen % wordlength)==0) {
            int inBitSetCount = inputlen / wordlength;
            info("Activating adder with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new boolean[inBitSetCount][wordlength];            
        } else return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the word length.");

        int iconstant = 0;
        if (args.length>1) {
            try {
                iconstant = Integer.decode(args[1]);
            } catch (NumberFormatException ne) {
                return error("Bad constant argument: " + args[1] + " expecting a number.");
            }
        } if (args.length>2) {
            if (args[2].equalsIgnoreCase("subtract")) {
                // subtract mode
                subtract = true;
            } else return error("Bad argument value: " + args[2]);
        }

        constant = BooleanArrays.fromInt(iconstant, wordlength); // TODO: support two's complement
        
        int maxResult = ((int)Math.pow(2, wordlength)-1) * inputBitSets.length + iconstant;
        int expectedOutputs = (int)Math.ceil(Math.log(maxResult)/Math.log(2));

        if (outputlen<expectedOutputs) 
            info(ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. Circuit should have " + expectedOutputs + " output bits.");
        
        return this;
    }

    public static boolean[] negate(boolean[] set, int length) {
        long n = BooleanArrays.toSignedInt(set, 0, length);
        return BooleanArrays.fromInt(-n, length);
    }
}

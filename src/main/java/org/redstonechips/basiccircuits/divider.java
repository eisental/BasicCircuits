package org.redstonechips.basiccircuits;


import org.bukkit.ChatColor;
import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class divider extends BitSetCircuit {
    int constant = 1;
    boolean round = false;
    boolean mod = false;

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        long firstOperand = BooleanArrays.toUnsignedInt(inputBitSets[0], 0, wordlength);
        long secondOperand = 1;
        if (inputBitSets.length>1) {
            for (int i=1; i<inputBitSets.length; i++) {
                long num = BooleanArrays.toUnsignedInt(inputBitSets[i], 0, wordlength);
                secondOperand = secondOperand * num;
            }
        }

        secondOperand = secondOperand * constant;

        if (!checkForDivByZero(secondOperand)) return;

        long result;
        if (round) {
            result = (int)Math.round((double)firstOperand / (double)secondOperand);
        } else {
            result = firstOperand / secondOperand;
        }

        this.writeInt(result, 0, wordlength);

        if (mod) {
            long modulus = firstOperand % secondOperand;
            this.writeInt(modulus, wordlength, outputlen-wordlength);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (args.length==0) return error("wordlength sign argument is missing.");
        if (outputlen==0) return error("Expecting at least 1 output pin.");

        if (args[args.length-1].equalsIgnoreCase("round"))
            round = true;
        else if (args[args.length-1].equalsIgnoreCase("mod"))
            mod = true;

        try {
            wordlength = Integer.decode(args[0]);

            if (wordlength<=0)
                return error("Bad wordlength sign argument: " + args[0] + ". Expecting a number greater than 0.");

        } catch (NumberFormatException ne) {
            return error("Bad wordlength sign argument: " + args[0] + ". Expecting a number greater than 0.");
        }

        if ((inputlen % wordlength)==0) {
            int inBitSetCount = inputlen / wordlength;
            info("Activating divider with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
            inputBitSets = new boolean[inBitSetCount][wordlength];
        } else {
            return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the word length.");
        }

        if ((args.length>1 && !(round || mod)) || (args.length>2)) {
            try {
                constant = Integer.decode(args[1]);
                if (constant==0) return error("Bad constant argument: " + args[0] + ". Division by zero is not allowed.");
            } catch (NumberFormatException ne) {
                return error("Bad constant argument: " + args[1] + " expecting a number.");
            }
        }

        int expectedOutputs = (mod?2*wordlength:wordlength);

        if (outputlen<expectedOutputs && activator!=null) {
            info(ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. To prevent this, the circuit should have " + expectedOutputs + " output bits.");
        }

        return this;
    }

    private boolean checkForDivByZero(long secondOperand) {
        if (secondOperand==0) {
            if (chip.hasListeners()) debug("Error: trying to divide by zero. ");
            return false;
        } else return true;
    }
}

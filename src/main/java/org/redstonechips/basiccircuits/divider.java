package org.redstonechips.basiccircuits;


import org.bukkit.ChatColor;
import org.redstonechips.chip.BitSetCircuit;
import org.redstonechips.chip.Circuit;
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
        int firstOperand = BooleanArrays.toUnsignedInt(inputBitSets[0], 0, wordlength);
        int secondOperand = 1;
        if (inputBitSets.length>1) {
            for (int i=1; i<inputBitSets.length; i++) {
                int num = BooleanArrays.toUnsignedInt(inputBitSets[i], 0, wordlength);
                secondOperand = secondOperand * num;
            }
        }

        secondOperand = secondOperand * constant;

        int result;
        if (round) {
            if (checkForDivByZero(secondOperand))
                result = (int)Math.round((double)firstOperand / (double)secondOperand);
            else return;
        } else {
            if (checkForDivByZero(secondOperand))
                result = firstOperand / secondOperand;
            else return;
            
        }
        this.writeInt(result, 0, wordlength);
        if (mod) {
            int modulus = firstOperand % secondOperand;
            this.writeInt(modulus, wordlength, outputlen-wordlength);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (args.length==0) return error("Wordlength sign argument is missing.");

        if (args.length==3) {
            if (args[2].equalsIgnoreCase("round"))
                round = true;
            else if (args[2].equalsIgnoreCase("mod"))
                mod = true;
            else return error("Unknown sign argument: " + args[1]);
        }

        if (outputlen==0) return error("Expecting at least 1 output pin.");

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

        if (args.length>1) {
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

    private boolean checkForDivByZero(int secondOperand) {
        if (secondOperand==0) {
            if (chip.hasListeners()) debug("Error: trying to divide by zero. ");
            return false;
        } else return true;
    }
}

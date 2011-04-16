package org.tal.basiccircuits;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.BitSetCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class divider extends BitSetCircuit {
    int constant = 1;
    boolean round = false;
    boolean mod = false;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet7 set) {
        int firstOperand = BitSetUtils.bitSetToUnsignedInt(inputBitSets[0], 0, wordlength);
        int secondOperand = 1;
        if (inputBitSets.length>1) {
            for (int i=1; i<inputBitSets.length; i++) {
                int num = BitSetUtils.bitSetToUnsignedInt(inputBitSets[i], 0, wordlength);
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
        this.sendInt(0, wordlength, result);
        if (mod) {
            int modulous = firstOperand % secondOperand;
            this.sendInt(wordlength, outputs.length-wordlength, modulous);
        }
    }

    @Override
    public boolean init(CommandSender sender, String[] args) {
        if (args.length==0) {
            error(sender, "Wordlength sign argument is missing.");
            return false;
        }

        if (args.length==3) {
            if (args[2].equalsIgnoreCase("round"))
                round = true;
            else if (args[2].equalsIgnoreCase("mod"))
                mod = true;
            else {
                error(sender, "Unknown sign argument: " + args[1]);
                return false;
            }
        }

        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }

        try {
            wordlength = Integer.decode(args[0]);

            if (wordlength<=0) {
                error(sender, "Bad wordlength sign argument: " + args[0] + ".");
                error(sender, "Expecting a number greater than 0.");

                return false;
            }
        } catch (NumberFormatException ne) {
            error(sender, "Bad wordlength sign argument: " + args[0] + ".");
            error(sender, "Expecting a number greater than 0.");

            return false;
        }

        if ((inputs.length % wordlength)==0) {
            int inBitSetCount = inputs.length / wordlength;
            info(sender, "Activating divider with " + inBitSetCount + " input set(s) of " + wordlength + " bits each.");
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

        int expectedOutputs = (mod?2*wordlength:wordlength);

        if (outputs.length<expectedOutputs && sender instanceof Player) {
            info(sender, ChatColor.LIGHT_PURPLE + "Warning: Output might overflow. To prevent this, the circuit should have " + expectedOutputs + " output bits.");
        }
        return true;    
    }

    private boolean checkForDivByZero(int secondOperand) {
        if (secondOperand==0) {
            if (hasDebuggers()) debug("Error: trying to divide by zero. ");
            return false;
        } else return true;
    }
}

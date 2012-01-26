
package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class comparator extends Circuit {
    private boolean hasConstant;
    private boolean identityMode;

    private int constant;

    private int wordLength;

    private int dataPin;
    private boolean clocked;
    
    private static final BitSet7 lessThan = BitSetUtils.intToBitSet(1, 3);
    private static final BitSet7 equals = BitSetUtils.intToBitSet(2, 3);
    private static final BitSet7 greaterThan = BitSetUtils.intToBitSet(4, 3);

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (clocked && inIdx==0 && state) calc();
        else if (!clocked) calc();
    }

    private void calc() {
        if (hasConstant) {
            // compare inputBits as an unsigned int to the constant.
            compare(BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, wordLength), constant);
        } else {
            compare(BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, wordLength),
                    BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin+wordLength, wordLength));
        }        
    }
    
    private void compare(int a, int b) {
        if (identityMode) {
            if (hasDebuggers()) debug(a + " is " + (a==b?"":"not ") + "equal to " + b);
            sendOutput(0, a==b);
        } else {
            if (a<b) {
                if (hasDebuggers()) debug(a + " is less than " + b);
                sendBitSet(lessThan);
            } else if (a==b) {
                if (hasDebuggers()) debug(a + " is equal to " + b);
                sendBitSet(equals);
            } else if (a>b) {
                if (hasDebuggers()) debug(a + " is greater than " + b);
                sendBitSet(greaterThan);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length==1) identityMode = true;
        else if (outputs.length==3) identityMode = false; // magnitude mode
        else {
            error(sender, "Expecting 1 output for an identity comparator or 3 outputs for a magnitude comparator");
            return false;
        }

        clocked = hasConstant = false;
        dataPin = 0;
        
        if (args.length>0) {
            clocked = args[args.length-1].equalsIgnoreCase("clockpin");
            dataPin = (clocked?1:0);

            if ((clocked && args.length>1) || !clocked) {
                // compare to a constant number
                try {
                    constant = Integer.decode(args[0]);
                    hasConstant = true;
                } catch (NumberFormatException ne) {
                    error(sender, "Bad constant argument: " + args[0]);
                    return false;
                }
            }
        } 

        if (!hasConstant) {
            if ((inputs.length-dataPin)%2!=0) {
                error(sender, "Expecting an even number of inputs when a constant is not used.");
                return false;
            }
            
            wordLength = (inputs.length-dataPin)/2;
        } else wordLength = inputs.length - dataPin;

        return true;
    }

}

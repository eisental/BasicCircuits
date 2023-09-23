
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

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

    private static final boolean[] lessThan = BooleanArrays.fromInt(1, 3);
    private static final boolean[] equals = BooleanArrays.fromInt(2, 3);
    private static final boolean[] greaterThan = BooleanArrays.fromInt(4, 3);

    @Override
    public void input(boolean state, int inIdx) {
        if (clocked && inIdx==0 && state) calc();
        else if (!clocked) calc();
    }

    private void calc() {
        if (hasConstant) {
            // compare inputBits as an unsigned int to the constant.
            compare(BooleanArrays.toUnsignedInt(inputs, dataPin, wordLength), constant);
        } else {
            compare(BooleanArrays.toUnsignedInt(inputs, dataPin, wordLength),
                    BooleanArrays.toUnsignedInt(inputs, dataPin+wordLength, wordLength));
        }
    }

    private void compare(long a, long b) {
        if (identityMode) {
            if (chip.hasListeners()) debug(a + " is " + (a==b?"":"not ") + "equal to " + b);
            write(a==b, 0);
        } else {
            if (a<b) {
                if (chip.hasListeners()) debug(a + " is less than " + b);
                writeBits(lessThan);
            } else if (a==b) {
                if (chip.hasListeners()) debug(a + " is equal to " + b);
                writeBits(equals);
            } else if (a>b) {
                if (chip.hasListeners()) debug(a + " is greater than " + b);
                writeBits(greaterThan);
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen==1) identityMode = true;
        else if (outputlen==3) identityMode = false; // magnitude mode
        else return error("Expecting 1 output for an identity comparator or 3 outputs for a magnitude comparator");

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
                    return error("Bad constant argument: " + args[0]);
                }
            }
        }

        if (!hasConstant) {
            if ((inputlen-dataPin)%2!=0)
                return error("Expecting an even number of inputs when a constant is not used.");

            wordLength = (inputlen-dataPin)/2;
        } else wordLength = inputlen - dataPin;

        return this;
    }
}

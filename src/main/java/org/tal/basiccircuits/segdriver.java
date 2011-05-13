
package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class segdriver extends Circuit {
    public final static BitSet7 zero = BitSetUtils.stringToBitSet("0111111");
    public final static BitSet7 one = BitSetUtils.stringToBitSet("0000110");
    public final static BitSet7 two = BitSetUtils.stringToBitSet("1011011");
    public final static BitSet7 three = BitSetUtils.stringToBitSet("1001111");
    public final static BitSet7 four = BitSetUtils.stringToBitSet("1100110");
    public final static BitSet7 five = BitSetUtils.stringToBitSet("1101101");
    public final static BitSet7 six = BitSetUtils.stringToBitSet("1111101");
    public final static BitSet7 seven = BitSetUtils.stringToBitSet("0000111");
    public final static BitSet7 eight = BitSetUtils.stringToBitSet("1111111");
    public final static BitSet7 nine = BitSetUtils.stringToBitSet("1101111");
    public final static BitSet7 a = BitSetUtils.stringToBitSet("1110111");
    public final static BitSet7 b = BitSetUtils.stringToBitSet("1111100");
    public final static BitSet7 c = BitSetUtils.stringToBitSet("0111001");
    public final static BitSet7 d = BitSetUtils.stringToBitSet("1011110");
    public final static BitSet7 e = BitSetUtils.stringToBitSet("1111001");
    public final static BitSet7 f = BitSetUtils.stringToBitSet("1110001");

    public final static BitSet7[] map = new BitSet7[] {zero, one, two, three, four, five, six, seven, eight, nine, a, b, c, d, e, f};

    private int blankPin = -1;
    private int dataPin = 0;
    private int clockPin = -1;

    private boolean blank = false;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==clockPin && state || (clockPin==-1 && inIdx>=dataPin)) {
            if (!blank)
            printInput();
        } else if (inIdx==blankPin) {
            blank = state;
            if (blank) {
                outputBits.clear();
                sendBitSet(outputBits);
                debug("Blanking display.");
            } else {
                printInput();
            }
        }
    }

    private String toSegmentLetters(BitSet7 segments) {
        String letters = "";
        for (int i=0; i<7; i++)
            if (segments.get(i)) letters += toSegmentLetter(i);

        return letters;
    }

    private String toSegmentLetter(int i) {
        switch (i) {
            case 0: return "a";
            case 1: return "b";
            case 2: return "c";
            case 3: return "d";
            case 4: return "e";
            case 5: return "f";
            case 6: return "g";
            default: throw new IllegalArgumentException("Bad segment id " + i);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length>0 && args[0].equalsIgnoreCase("blankPin"))
            blankPin = -2;

        if (outputs.length!=7) {
            error(sender, "Expecting 7 outputs (found " + outputs.length + ").");
            return false;
        }

        if (blankPin!=-1) {
            if (inputs.length<2 || inputs.length>6) {
                error(sender, "Expecting 2-6 inputs. Input pin 0 should be the blank pin.");
                return false;
            }

            if (inputs.length==2) { // no clock
                clockPin = -1;
                blankPin = 0;
                dataPin = 1;
            } else {
                clockPin = 0;
                blankPin = 1;
                dataPin = 2;
            }

            blank = inputBits.get(blankPin);

        } else {
            if (inputs.length<1 || inputs.length>5) {
                error(sender, "Expecting 1-5 inputs.");
                return false;
            }
            if (inputs.length==1) { // no clock
                clockPin = -1;
                blankPin = -1;
                dataPin = 0;
            } else {
                blankPin = -1;
                clockPin = 0;
                dataPin = 1;
            }

        }

        return true;
    }

    private void printInput() {
        int input = BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, inputs.length-dataPin);
        BitSet7 segments = map[input];
        if (hasDebuggers()) {
            debug("Printing " + Integer.toHexString(input) + ": " + toSegmentLetters(segments));
        }
        sendBitSet(segments);
    }

}

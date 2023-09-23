
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class segdriver extends Circuit {
    public final static boolean[] zero = BooleanArrays.fromString("0111111");
    public final static boolean[] one = BooleanArrays.fromString("0000110");
    public final static boolean[] two = BooleanArrays.fromString("1011011");
    public final static boolean[] three = BooleanArrays.fromString("1001111");
    public final static boolean[] four = BooleanArrays.fromString("1100110");
    public final static boolean[] five = BooleanArrays.fromString("1101101");
    public final static boolean[] six = BooleanArrays.fromString("1111101");
    public final static boolean[] seven = BooleanArrays.fromString("0000111");
    public final static boolean[] eight = BooleanArrays.fromString("1111111");
    public final static boolean[] nine = BooleanArrays.fromString("1101111");
    public final static boolean[] a = BooleanArrays.fromString("1110111");
    public final static boolean[] b = BooleanArrays.fromString("1111100");
    public final static boolean[] c = BooleanArrays.fromString("0111001");
    public final static boolean[] d = BooleanArrays.fromString("1011110");
    public final static boolean[] e = BooleanArrays.fromString("1111001");
    public final static boolean[] f = BooleanArrays.fromString("1110001");

    public final static boolean[][] map = new boolean[][] {zero, one, two, three, four, five, six, seven, eight, nine, a, b, c, d, e, f};

    private int blankPin = -1;
    private int dataPin = 0;
    private int clockPin = -1;

    private boolean blank = false;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==clockPin && state || (clockPin==-1 && inIdx>=dataPin)) {
            if (!blank)
            printInput();
        } else if (inIdx==blankPin) {
            blank = state;
            if (blank) {
                this.clearOutputs();
                debug("Blanking display.");
            } else {
                printInput();
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (args.length>0 && args[0].equalsIgnoreCase("blankPin"))
            blankPin = -2;

        if (outputlen!=7) return error("Expecting 7 outputs (found " + outputlen + ").");

        if (blankPin!=-1) {
            if (inputlen<2 || inputlen>6)
                return error("Expecting 2-6 inputs. Input pin 0 should be the blank pin.");

            if (inputlen==2) { // no clock
                clockPin = -1;
                blankPin = 0;
                dataPin = 1;
            } else {
                clockPin = 0;
                blankPin = 1;
                dataPin = 2;
            }
        } else {
            if (inputlen<1 || inputlen>5) return error("Expecting 1-5 inputs.");
            else if (inputlen==1) { // no clock
                clockPin = -1;
                blankPin = -1;
                dataPin = 0;
            } else {
                blankPin = -1;
                clockPin = 0;
                dataPin = 1;
            }

        }

        return this;
    }

    private void printInput() {
        int input = (int)BooleanArrays.toUnsignedInt(inputs, dataPin, inputlen-dataPin);
        boolean[] segments = map[input];
        if (chip.hasListeners()) {
            debug("Printing " + Integer.toHexString(input) + ": " + toSegmentLetters(segments));
        }
        writeBits(segments);
    }

    private String toSegmentLetters(boolean[] segments) {
        String letters = "";
        for (int i=0; i<7; i++)
            if (segments[i]) letters += toSegmentLetter(i);

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

}

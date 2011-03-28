/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class segdriver extends Circuit {
    public final static boolean[] zero = new boolean[] {true,true,true,true,true,true,false};
    public final static boolean[] one = new boolean[] {false,true,true,false,false,false,false};
    public final static boolean[] two = new boolean[] {true,true,false,true,true,false,true};
    public final static boolean[] three = new boolean[] {true,true,true,true,false,false,true};
    public final static boolean[] four = new boolean[] {false,true,true,false,false,true,true};
    public final static boolean[] five = new boolean[] {true,false,true,true,false,true,true};
    public final static boolean[] six = new boolean[] {true,false,true,true,true,true,true};
    public final static boolean[] seven = new boolean[] {true,true,true,false,false,false,false};
    public final static boolean[] eight = new boolean[] {true,true,true,true,true,true,true};
    public final static boolean[] nine = new boolean[] {true,true,true,true,false,true,true};
    public final static boolean[] a = new boolean[] {true,true,true,false,true,true,true};
    public final static boolean[] b = new boolean[] {false,false,true,true,true,true,true};
    public final static boolean[] c = new boolean[] {true,false,false,true,true,true,false};
    public final static boolean[] d = new boolean[] {false,true,true,true,true,false,true};
    public final static boolean[] e = new boolean[] {true,false,false,true,true,true,true};
    public final static boolean[] f = new boolean[] {true,false,false,false,true,true,true};

    public final static boolean[][] map = new boolean[][] { zero,one,two,three,four,five,six,seven,eight,nine,a,b,c,d,e,f };

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
        boolean[] segments = map[input];
        String seglist = "";
        for (int i=0; i<outputs.length; i++) {
            sendOutput(i, segments[i]);
            if (hasDebuggers() && segments[i]) seglist += toSegmentLetter(i);
        }

        if (hasDebuggers()) debug("Printing " + input + ": " + seglist);
    }

}

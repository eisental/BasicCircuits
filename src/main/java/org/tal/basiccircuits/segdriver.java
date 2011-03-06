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

    @Override
    public void inputChange(int inIdx, boolean state) {
        if ((inIdx==0 && state) || inputs.length==1) {
            int input = BitSetUtils.bitSetToUnsignedInt(inputBits, (inputs.length>1?1:0), inputs.length-(inputs.length>1?1:0));
            boolean[] segments = map[input];
            String seglist = "";
            for (int i=0; i<outputs.length; i++) {
                sendOutput(i, segments[i]);
                if (hasDebuggers() && segments[i]) seglist += toSegmentLetter(i);
            }

            if (hasDebuggers()) debug("Printing " + input + ": " + seglist);
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
        if (outputs.length!=7) {
            error(sender, "Expecting 7 outputs (found " + outputs.length + ").");
            return false;
        } else if (inputs.length<1 || inputs.length>5) {
            error(sender, "Expecting 1-5 outputs.");
            return false;
        }

        return true;
    }

}

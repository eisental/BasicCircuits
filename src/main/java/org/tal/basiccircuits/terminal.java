/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class terminal extends Circuit {
    private enum DataType { ascii, num }

    private BitSet7 outBuf = new BitSet7(8);
    private long[] buf = new long[1];
    private DataType type = DataType.ascii;

    @Override
    public void inputChange(int inIdx, boolean newLevel) { }

    @Override
    protected boolean init(Player player, String[] args) {
        if (args.length>0) {
            try {
                type = DataType.valueOf(args[0]);
            } catch (IllegalArgumentException ie) {
                error(player, "Unknown data type argument: " + args[0]);
            }
        }

        if (type==DataType.ascii) {
            if (outputs.length!=9) {
                error(player, "Expecting 9 outputs. 1 clock output and 8 data outputs.");
                return false;
            }
        } else if (type==DataType.num) {
            if (outputs.length<2) {
                error(player, "Expecting at least 2 outputs. 1 clock output and 1 or more data outputs.");
                return false;
            }
        }

        if (interfaceBlocks.length==0) {
            error(player, "Expecting at least one interface block.");
            return false;
        }

        for (Block i : interfaceBlocks)
            BasicCircuits.terminals.put(i, this);

        return true;
    }

    void type(String[] args, Player player) {
        String typeString = " ";
        for (String a : args)
            typeString += a + " ";

        if (type==DataType.ascii) {
            for (int i=0; i<typeString.length()-1; i++) {
                buf[0] = typeString.charAt(i);
                outBuf = BitSet7.valueOf(buf);
                this.sendBitSet(1, 8, outBuf);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            }
        } else if (type==DataType.num) {
            try {
                int i = Integer.decode(typeString.trim());
                buf[0] = i;
                outBuf = BitSet7.valueOf(buf);
                this.sendBitSet(1, outputs.length-1, outBuf);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            } catch (NumberFormatException ne) {
                error(player, "Not a number: " + typeString.trim() + ". use ascii data type for sending ascii symbols.");
            }
        }

    }
}


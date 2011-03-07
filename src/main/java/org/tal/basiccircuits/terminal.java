/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class terminal extends Circuit implements rcTypeReceiver {
    private enum DataType { ascii, num }

    private BitSet7 outBuf;
    private long[] buf = new long[1];
    private DataType type = DataType.ascii;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==0 && high) {
            // clear pin
            for (int i=0; i<outputs.length; i++)
                sendOutput(i, false);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length>0) {
            try {
                type = DataType.valueOf(args[0]);
            } catch (IllegalArgumentException ie) {
                error(sender, "Unknown data type argument: " + args[0]);
            }
        }

        if (type==DataType.ascii) {
            if (outputs.length!=9) {
                error(sender, "Expecting 9 outputs. 1 clock output and 8 data outputs.");
                return false;
            }
        } else if (type==DataType.num) {
            if (outputs.length<2) {
                error(sender, "Expecting at least 2 outputs. 1 clock output and 1 or more data outputs.");
                return false;
            }
        }

        if (interfaceBlocks.length==0) {
            error(sender, "Expecting at least one interface block.");
            return false;
        }

        for (Location l : interfaceBlocks)
            redstoneChips.registerRcTypeReceiver(l, this);

        return true;
    }

    @Override
    public void circuitDestroyed() {
        redstoneChips.removeRcTypeReceiver(this);
    }

    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) {
            error(player, "You didn't type anything after the command name.");
            return;
        }

        String typeString = "";

        for (String a : words)
            typeString += a + " ";
        if (type==DataType.ascii) {
            for (int i=0; i<typeString.length()-1; i++) {
                buf[0] = typeString.charAt(i);
                outBuf = BitSet7.valueOf(buf);
                if (hasDebuggers()) debug("Sending " + BitSetUtils.bitSetToBinaryString(outBuf, 0, 8));
                this.sendBitSet(1, 8, outBuf);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            }
        } else if (type==DataType.num) {
            try {
                int i = Integer.decode(typeString.trim());
                buf[0] = i;
                outBuf = BitSet7.valueOf(buf);
                if (hasDebuggers()) debug("Sending " + BitSetUtils.bitSetToBinaryString(outBuf, 0, outputs.length-1));
                this.sendBitSet(1, outputs.length-1, outBuf);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            } catch (NumberFormatException ne) {
                error(player, "Not a number: " + typeString.trim() + ". use ascii data type (1st sign arg), for sending ascii symbols.");
            }
        }

    }
}


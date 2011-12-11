
package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.InterfaceBlock;
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
    private boolean eot;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==0 && high) {
            // clear pin
            this.sendBitSet(BitSetUtils.clearBitSet);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        eot = false;

        if (args.length>0) {
            try {
                type = DataType.valueOf(args[0]);
            } catch (IllegalArgumentException ie) {
                error(sender, "Unknown data type argument: " + args[0]);
                return false;
            }

            if (args.length>1) {
                if (args[1].equalsIgnoreCase("eot")) {
                    eot = true;
                } else error(sender, "Unknodwn argument: " + args[1]);
            }
        }

        if (type==DataType.ascii) {
            if (outputs.length!=9 && outputs.length!=10) {
                error(sender, "Expecting 9-10 outputs. 1 clock output, an optional end-of-text output and 8 data outputs.");
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

        for (InterfaceBlock i : interfaceBlocks)
            redstoneChips.registerRcTypeReceiver(i.getLocation(), this);

        return true;
    }

    @Override
    public void circuitShutdown() {
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
            int datapin = (outputs.length==9?1:2);

            for (int i=0; i<typeString.length()-1; i++) {
                buf[0] = typeString.charAt(i);
                outBuf = BitSet7.valueOf(buf);
                if (hasDebuggers()) debug("Sending " + BitSetUtils.bitSetToBinaryString(outBuf, 0, 8));
                this.sendBitSet(datapin, 8, outBuf);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            }
           
            if (eot) {
                // send an EOT (end of text character - 0x3)
                this.sendInt(datapin, 8, 0x3);
                this.sendOutput(0, true);
                this.sendOutput(0, false);
            }

            if (datapin==2) {
                // pulse the EOT output pin if exists.
                this.sendOutput(1, true);
                this.sendOutput(1, false);
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

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String,String>();
        BitSetUtils.bitSetToMap(state, "outputBits", outputBits, outputs.length);
        return state;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        if (state.containsKey("outputBits")) {
            outputBits = BitSetUtils.mapToBitSet(state, "outputBits");
        }
    }
}


package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.channel.ReceivingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class receiver extends ReceivingCircuit {
    private int dataPin;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {}

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }
        
        if (args.length>0) {
            try {
                dataPin = (outputs.length==1?0:1);                
                this.initWireless(sender, args[0]);
                return true;
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
                return false;
            }
        } else {
            error(sender, "Channel name is missing.");
            return false;
        }
    }

    @Override
    public void receive(BitSet7 bits) {        
        if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, getChannelLength()));
        this.sendBitSet(dataPin, outputs.length-dataPin, bits);
        if (outputs.length>1) {
            this.sendOutput(0, true);
            this.sendOutput(0, false);
        }
    }

    @Override
    public int getChannelLength() {
        return outputs.length-dataPin;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }
}

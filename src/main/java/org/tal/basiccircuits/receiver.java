package org.tal.basiccircuits;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.channels.ReceivingCircuit;
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
                this.initWireless(args[0]);
                dataPin = (outputs.length==1?0:1);
                String bits;
                if (this.getLength()>1)
                    bits = "bits " + this.getStartBit() + "-" + (this.getStartBit() + this.getLength()-1);
                else bits = "bit " + this.getStartBit();

                info(sender, "Receiver will listen on channel " +
                        ChatColor.YELLOW + getChannel().name + redstoneChips.getPrefs().getInfoColor() + " " + bits + ".");

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
        if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, outputs.length));
        this.sendBitSet(dataPin, outputs.length-dataPin, bits);
        if (outputs.length>1) {
            this.sendOutput(0, true);
            this.sendOutput(0, false);
        }
    }

    @Override
    public void circuitShutdown() {
        if (getChannel()!=null) redstoneChips.removeReceiver(this);
    }

    @Override
    public int getLength() {
        return outputs.length-dataPin;
    }

}

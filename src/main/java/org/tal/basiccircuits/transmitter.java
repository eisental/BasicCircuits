package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.channels.TransmittingCircuit;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends TransmittingCircuit {
    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) { // no clock pin
            getChannel().transmit(inputBits, getStartBit(), inputs.length);
        } else { // has a clock pin
            if (inIdx==0 && high) { 
                getChannel().transmit(inputBits.get(1, inputs.length), getStartBit(), inputs.length-1);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            try {
                this.parseChannelString(args[0]);
                return true;
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
                return false;
            }
        } else {
            error(sender, "Channel sign argument is missing.");
            return false;
        }
    }

    @Override
    public void circuitShutdown() {
        redstoneChips.removeTransmitter(this);
    }

    @Override
    public int getLength() {
        if (inputs.length==1) return 1;
        else return inputs.length-1;
    }

}

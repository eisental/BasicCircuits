package org.tal.basiccircuits;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.channels.TransmittingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends TransmittingCircuit {
    boolean selectMode = false;
    int selectLength = 0;
    int baseStartBit;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) { // no clock pin and no select
            transmit(inputBits, getStartBit(), inputs.length);
        } else { // has a clock pin
            if (selectMode) {
                int select = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, selectLength);
                this.setStartBit(baseStartBit + select*getLength());
            }
            
            if (inputBits.get(0)) {
                transmit(inputBits.get(1+selectLength, inputs.length), getStartBit(), inputs.length-1-selectLength);
            }
        }
    }

    private void transmit(BitSet7 bits, int startBit, int length) {
        if (hasDebuggers()) debug("Transmitting " + BitSetUtils.bitSetToBinaryString(bits, 0, length) + " on " + getChannel().name + ":" + getStartBit());
        getChannel().transmit(bits, startBit, length);
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            try {
                this.initWireless(args[0]);
                baseStartBit = getStartBit();

                String bits;
                if (this.getLength()>1)
                    bits = "bits " + this.getStartBit() + "-" + (this.getStartBit() + this.getLength()-1);
                else bits = "bit " + this.getStartBit();

                if (args.length>1) {
                    selectMode = true;
                    try {
                        selectLength = Integer.decode(args[1]);
                        if (inputs.length<1+selectLength+1) {
                            error(sender, "Expecting atleast " + (2+selectLength) + " inputs for select mode.");
                        }
                    } catch (NumberFormatException ne) {
                        error(sender, "Bad select length argument: " + args[1]);
                        return false;
                    }
                }

                info(sender, "Transmitter will broadcast over channel " + 
                        ChatColor.YELLOW + getChannel().name + redstoneChips.getPrefs().getInfoColor() + " " + bits + ".");
                if (selectMode) {
                    info(sender, "Inputs 1-" + 1+selectLength + " are channel bit select pins.");
                }
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
        else {
            return inputs.length-1-selectLength;
        }
    }

}

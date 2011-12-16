package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.channel.TransmittingCircuit;
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
                this.setStartBit(baseStartBit + select*getChannelLength());
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
                if (args.length>1) {
                    String sselect;

                    if (!(args[1].toLowerCase().startsWith("select(") && args[1].toLowerCase().endsWith(")"))) {
                        error(sender, "Bad select length argument: " + args[1]);
                        return false;
                    } else sselect = args[1].substring(7, args[1].length()-1);

                    selectMode = true;
                    try {
                        selectLength = Integer.decode(sselect);
                        if (inputs.length<1+selectLength+1) {
                            error(sender, "Expecting at least " + (2+selectLength) + " inputs for select mode.");
                            return false;
                        }
                    } catch (NumberFormatException ne) {
                        error(sender, "Bad select length argument: " + args[1]);
                        return false;
                    }
                }

                try {
                    this.initWireless(sender, args[0]);
                } catch (IllegalArgumentException ie) {
                    error(sender, ie.getMessage());
                    return false;
                }
                
                baseStartBit = getStartBit();
                if (selectMode) {
                    info(sender, "Inputs 1-" + (selectLength) + " are channel bit select pins.");
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
    public int getChannelLength() {
        if (inputs.length==1) return 1;
        else {
            return inputs.length-1-selectLength;
        }
    }

}

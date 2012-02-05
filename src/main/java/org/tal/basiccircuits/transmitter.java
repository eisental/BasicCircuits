package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;
import org.tal.redstonechips.wireless.Transmitter;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit {
    boolean selectMode = false;
    int selectLength = 0;
    int baseStartBit;

    Transmitter trans;
    
    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) { // no clock pin and no select
            transmit(inputBits, 1);
        } else { // has a clock pin
            if (selectMode) {
                int select = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, selectLength);
                trans.setStartBit(baseStartBit + select*trans.getChannelLength());
            }
            
            if (inputBits.get(0)) {
                transmit(inputBits.get(1+selectLength, inputs.length), inputs.length-1-selectLength);
            }
        }
    }

    private void transmit(BitSet7 bits, int length) {
        if (hasDebuggers()) debug("Transmitting " + BitSetUtils.bitSetToBinaryString(bits, 0, length) + " on " + trans.getChannel().name + ":" + trans.getStartBit());
        trans.transmit(bits, length);
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
                    } else {
                        sselect = args[1].substring(7, args[1].length()-1);
                    }
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
                    trans = new Transmitter();
                    int len;
                    if (inputs.length==1) len = 1;
                    else len = inputs.length-1-selectLength;
                    

                    trans.init(sender, args[0], len, this);
                } catch (IllegalArgumentException ie) {
                    error(sender, ie.getMessage());
                    return false;
                }
                
                baseStartBit = trans.getStartBit();
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
<<<<<<< HEAD

    @Override
    public void circuitShutdown() {
        redstoneChips.removeTransmitter(this);
    }

    @Override
    public int getChannelLength() {
        if (inputs.length==1) return 1;
        else {
            return inputs.length-1-selectLength;
        }
    }
=======
>>>>>>> remotes/mainline/master
}

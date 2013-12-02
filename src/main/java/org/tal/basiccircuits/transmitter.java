package org.tal.basiccircuits;


import java.util.ArrayList;
import java.util.List;
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
    private boolean selectMode = false;
    private int selectLength = 0;
    private int[] baseStartBit;

    private Transmitter[] modules;
    
    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) { // no clock pin and no select
            transmit(inputBits, 1);
        } else { // has a clock pin
            if (selectMode) {
                int select = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, selectLength);
                for (int i=0; i<modules.length; i++) {
                    modules[i].setStartBit(baseStartBit[i] + select*modules[i].getChannelLength());
                }
                
            }
            
            if (inputBits.get(0)) {
                transmit(inputBits.get(1+selectLength, inputs.length), inputs.length-1-selectLength);
            }
        }
    }

    private void transmit(BitSet7 bits, int length) {
        if (hasListeners()) debug("Transmitting " + BitSetUtils.bitSetToBinaryString(bits, 0, length) + " on " + getChannelString());
        for (Transmitter t : modules)
            t.transmit(bits, length);
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            List<String> smodules = new ArrayList<String>();
                    
            try {
                for (String arg : args) {
                    if (arg.toLowerCase().startsWith("select(") && arg.toLowerCase().endsWith(")")) {
                        String sselect = arg.substring(7, arg.length()-1);
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
                        selectMode = true;
                    } else {                        
                        smodules.add(arg);
                    }                    
                }
                
                if (smodules.isEmpty()) {
                    error(sender, "Can't find any channel names.");
                    return false;
                }
                modules = new Transmitter[smodules.size()];
                baseStartBit = new int[modules.length];
                
                for (int i=0; i<modules.length; i++) {
                    try {
                        Transmitter t = new Transmitter();
                        int len;
                        if (inputs.length == 1)len = 1;
                        else len = inputs.length - 1 - selectLength;
                        t.init(sender, smodules.get(i), len, this);
                        modules[i]=t;
                        baseStartBit[i] = t.getStartBit();
                    } catch (IllegalArgumentException ie) {
                        error(sender, ie.getMessage());
                        return false;
                    }
                }
                
                
                debug("baseStartBit: " + baseStartBit);
                debug("modules: " + modules);
                
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
    
    private String getChannelString() {
        StringBuilder b = new StringBuilder();
        
        for (Transmitter t : modules) {
            b.append(t.getChannel().name).append(":").append(t.getStartBit()).append(", ");
        }
        b.delete(b.length()-2, b.length()-1);
        
        return b.toString();
    }
}

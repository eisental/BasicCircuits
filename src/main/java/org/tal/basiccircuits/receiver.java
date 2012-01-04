package org.tal.basiccircuits;


import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.wireless.Receiver;

/**
 *
 * @author Tal Eisenberg
 */
public class receiver extends Circuit {
    private int dataPin;
    private Receiver rec;
    
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
                rec = new ReceiverImpl();
                int len = outputs.length-dataPin;
                rec.init(sender, args[0], len, this);
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

    class ReceiverImpl extends Receiver {
        @Override
        public void receive(BitSet7 bits) {        
            if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, getChannelLength()));
            sendBitSet(dataPin, outputs.length-dataPin, bits);
            if (outputs.length>1) {
                sendOutput(0, true);
                sendOutput(0, false);
            }
        }        
    }

    @Override
    protected boolean isStateless() {
        return false;
    }

    @Override
    public void circuitShutdown() {
        if (rec!=null) rec.shutdown();
    }    
}

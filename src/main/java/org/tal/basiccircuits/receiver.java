package org.tal.basiccircuits;


import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.ReceivingCircuit;
import org.tal.redstonechips.circuit.TransmittingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class receiver extends Circuit implements ReceivingCircuit {
    private String channel;
    private int dataPin;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {}

    @Override
    protected boolean init(Player player, String[] args) {
        if (args.length>0) {
            channel = args[0];

            // register the receiver
            redstoneChips.addReceiver(this);

            dataPin = (outputs.length==1?0:1);
            
            return true;
        } else {
            error(player, "Channel name is missing.");
            return false;
        }


    }

    @Override
    public void receive(BitSet7 bits) {
        if (hasDebuggers()) debug("received " + BitSetUtils.bitSetToBinaryString(bits, 0, outputs.length));
        this.sendBitSet(dataPin, outputs.length-dataPin, bits);
        if (outputs.length>1) {
            this.sendOutput(0, true);
            this.sendOutput(0, false);
        }
    }

    @Override
    public String getChannel() { return channel; }

    @Override
    public void circuitDestroyed() {
        redstoneChips.receivers.remove(this);
        for (TransmittingCircuit t: redstoneChips.transmitters) {
            if (t.getChannel()!=null && t.getChannel().equals(channel)) t.removeReceiver(this);
        }
    }
}

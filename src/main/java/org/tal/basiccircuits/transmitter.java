package org.tal.basiccircuits;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit {
    private List<receiver> receivers = new ArrayList<receiver>();
    private String channel;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) { // no clock pin
            transmitBitSet(inputBits, 0, inputs.length);
        } else { // has a clock pin
            if (inIdx==0 && high) { 
                transmitBitSet(inputBits, 1, inputs.length-1);
            }
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length==0) {
            error(player, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            channel = args[0];

            // register the transmitter
            BasicCircuits.transmitters.add(this);

            // find already existing receivers for this channel
            for (receiver r : BasicCircuits.receivers) {
                if (r.getChannel()!=null && r.getChannel().equals(channel)) addReceiver(r);
            }

            return true;
        } else {
            error(player, "Channel sign argument is missing.");
            return false;
        }
    }

    public void addReceiver(receiver r) { 
        receivers.add(r);
    }

    public void removeReceiver(receiver r) {
        receivers.remove(r);
    }

    public String getChannel() { return channel; }

    @Override
    public void circuitDestroyed() {
        BasicCircuits.transmitters.remove(this);
    }

    private void transmitBitSet(BitSet7 bits, int startBit, int length) {
        BitSet7 tbits = bits.get(startBit, length+startBit);
        if (hasDebuggers()) debug("Transmitting " + bitSetToBinaryString(tbits, 0, length));
        for (receiver r : receivers) {
            r.receive(tbits);
        }
    }
}

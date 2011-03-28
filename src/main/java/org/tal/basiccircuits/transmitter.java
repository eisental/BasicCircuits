package org.tal.basiccircuits;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.ReceivingCircuit;
import org.tal.redstonechips.circuit.TransmittingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit implements TransmittingCircuit {
    private List<ReceivingCircuit> receivers = new ArrayList<ReceivingCircuit>();
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
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input.");
            return false;
        }
        if (args.length>0) {
            channel = args[0];

            // register the transmitter
            redstoneChips.addTransmitter(this);

            // find already existing receivers for this channel
            for (ReceivingCircuit r : redstoneChips.getReceivers()) {
                if (r.getChannel()!=null && r.getChannel().equals(channel)) addReceiver(r);
            }

            return true;
        } else {
            error(sender, "Channel sign argument is missing.");
            return false;
        }
    }

    @Override
    public void addReceiver(ReceivingCircuit r) {
        receivers.add(r);
    }

    @Override
    public void removeReceiver(ReceivingCircuit r) {
        receivers.remove(r);
    }

    @Override
    public String getChannel() { return channel; }

    @Override
    public void circuitShutdown() {
        redstoneChips.removeTransmitter(this);
    }

    private void transmitBitSet(BitSet7 bits, int startBit, int length) {
        BitSet7 tbits = bits.get(startBit, length+startBit);
        if (hasDebuggers()) debug("Transmitting " + BitSetUtils.bitSetToBinaryString(tbits, 0, length) + " to " + receivers.size() + " receiver(s).");
        for (ReceivingCircuit r : receivers) {
            r.receive(tbits);
        }
    }
}

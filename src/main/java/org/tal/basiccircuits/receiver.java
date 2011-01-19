package org.tal.basiccircuits;


import java.util.BitSet;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class receiver extends Circuit {
    private String channel;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {}

    @Override
    protected boolean init(Player player, String[] args) {
        if (args.length>0) {
            channel = args[0];

            // register the transmitter
            BasicCircuits.receivers.add(this);

            // find already existing transmitters for this channel
            for (transmitter t : BasicCircuits.transmitters) {
                if (t.getChannel()!=null && t.getChannel().equals(channel)) t.addReceiver(this);
            }

            return true;
        } else {
            player.sendMessage("Channel name is missing.");
            return false;
        }

    }

    public void receive(BitSet bits) {
        this.sendBitSet(bits);
    }

    public String getChannel() { return channel; }

    @Override
    public void circuitDestroyed() {
        BasicCircuits.receivers.remove(this);
        for (transmitter t: BasicCircuits.transmitters) {
            if (t.getChannel()!=null && t.getChannel().equals(channel)) t.removeReceiver(this);
        }
    }
}

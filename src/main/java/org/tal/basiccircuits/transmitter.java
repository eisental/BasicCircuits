package org.tal.basiccircuits;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit {
    private List<receiver> receivers = new ArrayList<receiver>();
    private String channel;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        for (receiver r : receivers) {
            r.receive(inputBits);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
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
}

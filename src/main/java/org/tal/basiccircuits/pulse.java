package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class pulse extends Circuit {
    enum EdgeTriggering { positive, negative, doubleEdge };

    EdgeTriggering trigger = EdgeTriggering.positive;

    private long interval;

    @Override
    public void inputChange(final int inIdx, boolean high) {
        if (high && (trigger==EdgeTriggering.positive || trigger==EdgeTriggering.doubleEdge))
            pulse(inIdx);
        else if (!high && (trigger==EdgeTriggering.negative || trigger==EdgeTriggering.doubleEdge))
            pulse(inIdx);

    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        }

        if (inputs.length==0) {
            error(player, "Expecting at least one input and one output.");
        }

        if (args.length==0) interval = 1000; // 1 sec default
        if (args.length>=1)
            interval = Math.round(UnitParser.parse(args[0]));
        if (args.length>=2) {
            if (args.length==2) {
                try {
                    trigger = EdgeTriggering.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    error(player, "Bad trigger argument: " + args[1]);
                }
            }
        }

        return true;
    }

    private void pulse(final int inIdx) {
        sendOutput(inIdx, true);
        if (interval==0) {
            sendOutput(inIdx, false);
        } else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(interval);
                    } catch (InterruptedException ex) {}
                    sendOutput(inIdx, false);
                }
            }.start();
        }

    }
}

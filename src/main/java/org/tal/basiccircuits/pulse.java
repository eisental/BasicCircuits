package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class pulse extends Circuit {
    public enum EdgeTriggering { positive, negative, doubleEdge };

    private EdgeTriggering trigger = EdgeTriggering.positive;

    private long interval;
    private PulseOff[] pulseOffs;
    private long intervalInTicks;

    @Override
    public void inputChange(final int inIdx, boolean high) {
        if (high && (trigger==EdgeTriggering.positive || trigger==EdgeTriggering.doubleEdge))
            pulse(inIdx);
        else if (!high && (trigger==EdgeTriggering.negative || trigger==EdgeTriggering.doubleEdge))
            pulse(inIdx);

    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=outputs.length) {
            error(sender, "Expecting the same number of inputs and outputs.");
            return false;
        }

        if (inputs.length==0) {
            error(sender, "Expecting at least one input and one output.");
        }

        if (args.length==0) interval = 1000; // 1 sec default
        
        if (args.length>=1) {
            try {
                interval = Math.round(UnitParser.parse(args[0]));
            } catch (Exception e) {
                error(sender, "Bad pulse duration argument: " + args[0]);
                return false;
            }
        }

        if (args.length>=2) {
            if (args.length==2) {
                try {
                    trigger = EdgeTriggering.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    error(sender, "Bad trigger argument: " + args[1]);
                }
            }
        }

        if (interval!=0) {
            intervalInTicks = Math.round(interval/50);

            pulseOffs = new PulseOff[outputs.length];
            for (int i=0; i < outputs.length; i++) {
                pulseOffs[i] = new PulseOff(i);
            }
        }

        return true;
    }

    private void pulse(final int inIdx) {
        sendOutput(inIdx, true);
        if (interval==0) {
            sendOutput(inIdx, false);
        } else {
            redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, pulseOffs[inIdx], intervalInTicks);
        }

    }

    class PulseOff implements Runnable {
        int index;

        public PulseOff(int index) { this.index = index; }

        @Override
        public void run() {
            sendOutput(index, false);
        }

    }
}

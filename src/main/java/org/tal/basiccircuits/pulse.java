package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import net.eisental.common.parsing.UnitParser;

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
    public void inputChange(final int inIdx, boolean on) {
        if (on && (trigger==EdgeTriggering.positive || trigger==EdgeTriggering.doubleEdge)) {
            if (inputs.length==outputs.length)
                pulse(inIdx);
            else pulseSequence();
        } else if (!on && (trigger == EdgeTriggering.negative || trigger == EdgeTriggering.doubleEdge)) {
            if (inputs.length==outputs.length)
                pulse(inIdx);
            else pulseSequence();
        }

    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=outputs.length && inputs.length!=1) {
            error(sender, "Expecting the same number of inputs and outputs or 1 input.");
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

    private void pulse(final int idx) {
        sendOutput(idx, true);
        if (interval==0) {
            sendOutput(idx, false);
        } else {
            redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, pulseOffs[idx], intervalInTicks);
        }

    }

    private void pulseSequence() {
        if (interval==0) {
            for (int i=0; i<outputs.length; i++) {
                sendOutput(i, true);
                sendOutput(i, false);
            }
        } else {
            sendOutput(0, true);
            redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, pulseOffs[0], intervalInTicks);
        }
    }

    class PulseOff implements Runnable {
        int index;

        public PulseOff(int index) { this.index = index; }

        @Override
        public void run() {
            sendOutput(index, false);
            if (inputs.length!=outputs.length && pulseOffs.length>index+1) {
                sendOutput(index+1, true);
                redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, pulseOffs[index+1], intervalInTicks);
            }
        }

    }
}

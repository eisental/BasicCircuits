package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.parsing.UnitParser;

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
    public void input(boolean state, final int inIdx) {
        if (state && (trigger==EdgeTriggering.positive || trigger==EdgeTriggering.doubleEdge)) {
            if (inputlen==outputlen)
                pulse(inIdx);
            else pulseSequence();
        } else if (!state && (trigger == EdgeTriggering.negative || trigger == EdgeTriggering.doubleEdge)) {
            if (inputlen==outputlen)
                pulse(inIdx);
            else pulseSequence();
        }

    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen!=outputlen && inputlen!=1)
            return error("Expecting the same number of inputs and outputs or 1 input.");

        if (inputlen==0) 
            return error("Expecting at least one input and one output.");
        

        if (args.length==0) interval = 1000; // 1 sec default
        
        if (args.length>=1) {
            try {
                interval = Math.round(UnitParser.parse(args[0]));
            } catch (IllegalArgumentException e) {
                return error("Bad pulse duration argument: " + args[0]);
            }
        }

        if (args.length>=2) {
            if (args.length==2) {
                try {
                    trigger = EdgeTriggering.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    return error("Bad trigger argument: " + args[1]);
                }
            }
        }

        if (interval!=0) {
            intervalInTicks = Math.round(interval/50);

            pulseOffs = new PulseOff[outputlen];
            for (int i=0; i < outputlen; i++) {
                pulseOffs[i] = new PulseOff(i);
            }
        }

        return this;
    }

    private void pulse(final int idx) {
        write(true, idx);
        if (interval==0) {
            write(false, idx);
        } else {
            rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[idx], intervalInTicks);
        }

    }

    private void pulseSequence() {
        if (interval==0) {
            for (int i=0; i<outputlen; i++) {
                write(true, i);
                write(false, i);
            }
        } else {
            write(true, 0);
            rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[0], intervalInTicks);
        }
    }

    class PulseOff implements Runnable {
        int index;

        public PulseOff(int index) { this.index = index; }

        @Override
        public void run() {
            write(false, index);
            if (inputlen!=outputlen && pulseOffs.length>index+1) {
                write(true, index+1);
                rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[index+1], intervalInTicks);
            }
        }
    }
}

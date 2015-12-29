package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.parsing.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class pulse extends Circuit {

    public enum EdgeTriggering { positive, negative, doubleEdge };
    public enum RepeatedTriggerMode { legacy, ignore, extend };

    private EdgeTriggering trigger = EdgeTriggering.positive;
    private RepeatedTriggerMode repeatedTriggerMode = RepeatedTriggerMode.legacy;

    private long interval;
    private PulseOff[] pulseOffs;
    private Integer[] pulseOffIds;
    private long intervalInTicks;

    @Override
    public void input(boolean state, final int inIdx) {
        if (state && (trigger==EdgeTriggering.positive || trigger==EdgeTriggering.doubleEdge)) {
            trigger(inIdx);
        } else if (!state && (trigger == EdgeTriggering.negative || trigger == EdgeTriggering.doubleEdge)) {
            trigger(inIdx);
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
            try {
                trigger = EdgeTriggering.valueOf(args[1]);
            } catch (IllegalArgumentException ie) {
                return error("Bad trigger argument: " + args[1]);
            }
        }
        
        if (args.length>=3) {
            try {
                repeatedTriggerMode = RepeatedTriggerMode.valueOf(args[2]);
            } catch (IllegalArgumentException e) {
                return error("Bad repeated trigger mode argument: " + args[2]);
            }
        }
        
        if (repeatedTriggerMode==RepeatedTriggerMode.extend && inputlen!=outputlen) {
            return error("Extend mode is incompatible with sequence mode");
        }

        if (interval!=0) {
            intervalInTicks = Math.round(interval/50);
            pulseOffIds = new Integer[outputlen];

            pulseOffs = new PulseOff[outputlen];
            for (int i=0; i < outputlen; i++) {
                pulseOffs[i] = new PulseOff(i, pulseOffIds);
            }
        }

        return this;
    }
    
    private void trigger(final int idx) {
        int checkRepeatIdx = 0;
        
        if (repeatedTriggerMode==RepeatedTriggerMode.legacy) {
            pulse(idx);
        }
        else {
            if (inputlen==outputlen) {
                checkRepeatIdx = idx;
            }
            if (repeatedTriggerMode==RepeatedTriggerMode.ignore) {
                if (pulseOffIds[checkRepeatIdx]==null) {
                    pulse(idx);
                }
            }
            else if (repeatedTriggerMode==RepeatedTriggerMode.extend) {
                if (pulseOffIds[checkRepeatIdx]!=null) {
                    rc.getServer().getScheduler().cancelTask(pulseOffIds[checkRepeatIdx]);
                    pulseOffIds[checkRepeatIdx] = null;
                }
                pulse(idx);
            }
        }
    }
    
    private void pulse(final int idx) {
        if (inputlen==outputlen)
            pulseSingle(idx);
        else pulseSequence();
    }

    private void pulseSingle(final int idx) {
        write(true, idx);
        if (interval==0) {
            write(false, idx);
        } else {
            pulseOffIds[idx] = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[idx], intervalInTicks);
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
            pulseOffIds[0] = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[0], intervalInTicks);
        }
    }

    class PulseOff implements Runnable {
        int index;
        Integer[] pulseOffIds;

        public PulseOff(int index, Integer[] pulseOffIds) {
            this.index = index;
            this.pulseOffIds = pulseOffIds;
        }

        @Override
        public void run() {
            write(false, index);
            if (inputlen==outputlen || index==0) {
                pulseOffIds[index] = null;
            }
            if (inputlen!=outputlen && pulseOffs.length>index+1) {
                write(true, index+1);
                rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, pulseOffs[index+1], intervalInTicks);
            }
        }
    }
}

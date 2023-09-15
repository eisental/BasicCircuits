
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.parsing.UnitParser;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class burst extends Circuit {
    long pulseCount;
    long frequency = 0;
    boolean bursting;
    int taskId = -1;
    Runnable burstTask;
    long expectedNextTick;
    boolean currentState = true;
    int currentcount = 0;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0 && state) {
            if (frequency == 0) {
                pulse(pulseCount);
            } else {
                if (!bursting) {
                    startBurst();
                }
            }
        } else if (inIdx>0) {
            pulseCount = BooleanArrays.toUnsignedInt(inputs, 1, inputlen-1);
        }
    }

    private void pulse(long count) {
        if (chip.hasListeners()) debug("Pulsing " + count + " time(s).");
        for (int i=0; i<count; i++) {
            for (int m=0; m<outputlen; m++) {
                write(true, m);
                write(false, m);
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (args.length>0) {
            if (args.length == 1) {
                try {
                    pulseCount = Integer.decode(args[0]);
                    if (pulseCount<0) return error("Bad pulse count argument: " + args[0]);
                } catch (NumberFormatException ne) {
                    try {
                        frequency = Math.round(UnitParser.parse(args[0]));
                    } catch (IllegalArgumentException e) {
                        return error("Bad parameter: " + args[0]);
                    }
                }
            } else if (args.length == 2) {
                try {
                    pulseCount = Integer.decode(args[0]);
                    if (pulseCount<0) return error("Bad pulse count argument: " + args[0]);
                } catch (NumberFormatException ne) {
                    return error("Bad pulse count argument: " + args[0]);
                }

                try {
                    frequency = Math.round(UnitParser.parse(args[1]));
                } catch (IllegalArgumentException e) {
                    return error("Bad burst frequency: " + args[1]);
                }
            }
        }

        if (outputlen==0) return error("Expecting at least 1 output pin.");
        if (inputlen==0) return error("Expecting at least 1 input pin.");

        if (activator!=null) clearOutputs();

        burstTask = new BurstTask();
        return this;
    }

    @Override
    public boolean isStateless() {
        return false;
    }

    private void startBurst() {
        if (bursting) return;

        if (chip.hasListeners()) debug("Starting Burst.");

        bursting = true;
        currentcount = 0;
        taskId = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, burstTask);
        if (taskId==-1) {
            if (chip.hasListeners()) debug("Burst task schedule failed!");
            bursting = false;
        }
    }

    private void stopBurst() {
        if (!bursting) return;

        if (chip.hasListeners()) debug("Stopping Burst.");

        rc.getServer().getScheduler().cancelTask(taskId);
        bursting = false;
    }

    @Override
    public void disable() {
        stopBurst();
    }

    @Override
    public void shutdown() {
        stopBurst();
    }

    private class BurstTask implements Runnable {
        @Override
        public void run() {
            if (!bursting) return;
            if (currentcount == pulseCount * 2) {
                bursting = false;
                return;
            }

            tick();

            long delay = Math.round(frequency/2);

            long correction = 0;
            if (expectedNextTick!=-1) {
                correction = expectedNextTick - System.currentTimeMillis();
            }

            if (correction<=-delay) correction = correction % delay;
            delay += correction;
            expectedNextTick = System.currentTimeMillis() + delay;
            long tickDelay = Math.round(delay/50.0);

            int id = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, burstTask, tickDelay);

            if (id!=-1) {
                taskId = id;
            } else if (chip.hasListeners()) debug("Burst task schedule failed!");

            currentState = !currentState;
            currentcount += 1;
        }

        private void tick() {
            for (int m=0; m<outputlen; m++) {
                write(currentState, m);
            }
        }
    }
}

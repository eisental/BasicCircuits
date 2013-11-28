
package org.tal.basiccircuits;

import net.eisental.common.parsing.UnitParser;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.bitset.BitSetUtils;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class burst extends Circuit {
    int pulseCount;
    long frequency = 0;
    boolean bursting;
    int taskId = -1;
    Runnable burstTask;
    long expectedNextTick;
    boolean currentState = true;
    int currentcount = 0;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==0 && state) {
            if (frequency == 0) {
                pulse(pulseCount);
            } else {
                if (!bursting) {
                    startBurst();
                }
            }
        } else if (inIdx>0) {
            pulseCount = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
        }
    }

    private void pulse(int count) {
        if (hasDebuggers()) debug("Pulsing " + count + " time(s).");
        for (int i=0; i<count; i++) {
            for (int m=0; m<outputs.length; m++) {
                sendOutput(m, true);
                sendOutput(m, false);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length>0) {
            if (args.length == 1) {
                try {
                    pulseCount = Integer.decode(args[0]);
                    if (pulseCount<0) {
                        error(sender, "Bad pulse count argument: " + args[0]);
                        return false;
                    }
                } catch (NumberFormatException ne) {
                    try {
                        frequency = Math.round(UnitParser.parse(args[0]));
                    } catch (Exception e) {
                        error(sender, "Bad parameter: " + args[0]);
                        return false;
                    }
                }
            } else if (args.length == 2) {
                try {
                    pulseCount = Integer.decode(args[0]);
                    if (pulseCount<0) {
                        error(sender, "Bad pulse count argument: " + args[0]);
                        return false;
                    }
                } catch (NumberFormatException ne) {
                    error(sender, "Bad pulse count argument: " + args[0]);
                    return false;
                }
            
                try {
                    frequency = Math.round(UnitParser.parse(args[1]));
                } catch (Exception e) {
                    error(sender, "Bad burst frequency: " + args[1]);
                    return false;
                }
            }
        }

        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }

        if (inputs.length==0) {
            error(sender, "Expecting at least 1 input pin.");
            return false;
        }

        if (sender!=null) resetOutputs();
        
        burstTask = new BurstTask();
        return true;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }
    
    private void startBurst() {
        if (bursting) return;

        if (hasDebuggers()) debug("Starting Burst.");

        bursting = true;
        currentcount = 0;
        taskId = redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, burstTask);
        if (taskId==-1) {
            if (hasDebuggers()) debug("Burst task schedule failed!");
            bursting = false;
        }
    }
    
    private void stopBurst() {
        if (!bursting) return;

        if (hasDebuggers()) debug("Stopping Burst.");

        redstoneChips.getServer().getScheduler().cancelTask(taskId);
        bursting = false;
    }
    
    @Override
    public void disable() {
        super.disable();
        stopBurst();
    }
    
    @Override
    public void circuitShutdown() {
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

            int id = redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, burstTask, tickDelay);

            if (id!=-1) {
                taskId = id;
            } else {
                if (hasDebuggers()) {
                    debug("Burst task schedule failed!");
                }
            }
            currentState = !currentState;
            currentcount += 1;
        }

        private void tick() {
            for (int m=0; m<outputs.length; m++) {
                sendOutput(m, currentState);
            }
        }
    }
}

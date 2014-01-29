
package org.redstonechips.basiccircuits;

import java.util.BitSet;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.parsing.UnitParser;
import org.redstonechips.wireless.Transmitter;

/**
 *
 * @author Tal Eisenberg
 */
public class clock extends Circuit {
    private long onDuration, offDuration;
    private boolean masterToggle;
    private BitSet onBits, offBits;
    private Runnable tickTask;
    private boolean ticking;
    private long expectedNextTick;
    private int taskId = -1;

    private Transmitter transmitter;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (masterToggle) {
            if (inIdx==0) {
                if (state) startClock();
                else {
                    stopClock();
                    writeBitSet(offBits);
                    if (transmitter!=null) transmitter.transmit(false);
                }
            }
        } else {
            onBits.set(inIdx, state);

            if (onBits.isEmpty()) {
                stopClock();
                writeBitSet(offBits);
                if (transmitter!=null) transmitter.transmit(false);
            }
            else startClock();
        }
    }

    @Override
    public Circuit init(String[] args) {
        String channelArg = null;
        if (args.length>0 && args[args.length-1].startsWith("#")) {
            // last argument is a channel name
            channelArg = args[args.length-1].substring(1);
            String[] newArgs = new String[args.length-1];
            if (newArgs.length>0)
                System.arraycopy(args, 0, newArgs, 0, newArgs.length);

            args = newArgs;
        }

        if (args.length==0) setDuration(1000, 0.5); // 1 sec default, 50% pulse width
        else {
            double pulseWidth = 0.5;
            if (args.length>1) {
                try {
                    pulseWidth = Double.parseDouble(args[1]);
                } catch (NumberFormatException ne) {
                    return error("Bad pulse width: " + args[1] + ". Expecting a number between 0 and 1.0");
                }
            }

            try {
                long freq = Math.round(UnitParser.parse(args[0]));
                setDuration(freq, pulseWidth);
            } catch (IllegalArgumentException e) {
                return error("Bad clock frequency: " + args[0]);
            }
        }

        if (inputlen!=1 && inputlen!=outputlen) {
            return error("Expecting the same amount of inputs and outputs or 1 input to control all outputs.");
        }

        offBits = new BitSet(outputlen);
        offBits.set(0, outputlen, false);

        onBits = new BitSet(outputlen);

        if (inputlen==1) {
            masterToggle = true;
            onBits.set(0, outputlen);
        }

        if ((onDuration<50 && onDuration>0) || (offDuration<50 && offDuration>0))
            return error("Clock frequency is currently limited to 50ms per state. Use a lower freq argument or try setting pulse-width to 0.");        

        if (channelArg!=null) {
            try {
                transmitter = new Transmitter();
                transmitter.init(activator, channelArg, 1, this);
            } catch (IllegalArgumentException ie) {
                return error(ie.getMessage());
            }
        }
        info("The clock will tick every " + (onDuration+offDuration) + " milliseconds for " + onDuration + " milliseconds.");

        tickTask = new TickTask();

        expectedNextTick = -1;
        ticking = false;

        return this;
    }

    private void setDuration(long duration, double pulseWidth) {
        this.onDuration = Math.round(duration*pulseWidth);
        this.offDuration = duration - onDuration;
    }

    private void startClock() {
        if (ticking) return;

        if (chip.hasListeners()) debug("Turning clock on.");

        ticking = true;
        currentState = true;
        
        taskId = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, tickTask);
        if (taskId==-1) {
            if (chip.hasListeners()) debug("Tick task schedule failed!");
            ticking = false;
        }
    }

    private void stopClock() {
        if (!ticking) return;

        if (chip.hasListeners()) debug("Turning clock off.");

        rc.getServer().getScheduler().cancelTask(taskId);
        ticking = false;
    }

    @Override
    public void disable() {
        super.disable();
        stopClock();
    }   

    @Override
    public void enable() {
        super.enable();
        if (masterToggle) {
            if (inputs[0]) startClock();
        } else if (!onBits.isEmpty())
            startClock();
    }
    
    @Override
    public void shutdown() {
        stopClock();
    }

    boolean currentState = true;
    
    private class TickTask implements Runnable {
        

        @Override
        public void run() {
            if (!ticking) return;

            tick();

            long delay;

            if (onDuration==0)
                delay = offDuration;
            else if(offDuration == 0)
                delay = onDuration;
            else
                delay = (currentState?onDuration:offDuration);

            long correction = 0;
            if (expectedNextTick!=-1) {
                correction = expectedNextTick - System.currentTimeMillis();
            }

            if (correction<=-delay) correction = correction % delay;
            delay += correction;
            expectedNextTick = System.currentTimeMillis() + delay;
            long tickDelay = Math.round(delay/50.0);

            int id = rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, tickTask, tickDelay);

            if (id!=-1) {
                taskId = id;
            } else {
                if (chip.hasListeners()) {
                    debug("Tick task schedule failed!");
                }
            }

            currentState = !currentState;
        }

        private void tick() {
            if (onDuration>0 && offDuration>0) {
                writeBitSet(currentState?onBits:offBits);
                if (transmitter!=null) transmitter.transmit(currentState);
            } else if (onDuration > 0) {
                writeBitSet(offBits);
                if (transmitter!=null) transmitter.transmit(false);
                writeBitSet(onBits);
                if (transmitter!=null) transmitter.transmit(true);
            } else if (offDuration>0) {
                writeBitSet(onBits);
                if (transmitter!=null) transmitter.transmit(true);
                writeBitSet(offBits);
                if (transmitter!=null) transmitter.transmit(false);
            }
        }
    }
}

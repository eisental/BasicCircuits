package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class clock extends Circuit {
    private long interval;
    private long onInterval, offInterval;

    private boolean running = false;
    private BitSet7 onBits, offBits;
    private Thread thread;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (state) { // one of the input pins is turned on.
            if (!running) startClock();
        } else { // one of the input pins is set to off.
            if (running && inputBits.isEmpty()) stopClock();
        }
    }

    @Override
    public boolean init(CommandSender sender, String[] args) {
        // one argument for duration. number of inputs should match number of outputs.
        double pulseWidth = 0.5;
        if (args.length==0) setFreq(1000, pulseWidth); // 1 sec default, 50% pulse width
        else {
            if (args.length>1) {
                try {
                    pulseWidth = Double.parseDouble(args[1]);
                } catch (NumberFormatException ne) {
                    error(sender, "Bad pulse width: " + args[1] + ". Expecting a number between 0 and 1.0");
                    return false;
                }
            }

            try {
                long freq = Math.round(UnitParser.parse(args[0]));
                setFreq(freq, pulseWidth);
            } catch (Exception e) {
                error(sender, "Bad clock frequency: " + args[0]);
                return false;
            }
        }

        if (inputs.length!=outputs.length) {
            error(sender, "Expecting the same amount of inputs and outputs.");
            return false;
        }

        onBits = new BitSet7(inputs.length);
        offBits = new BitSet7(inputs.length);

        onBits.set(0, inputs.length);
        offBits.clear();
        if (interval<100) {
            error(sender, "Clock is set to tick too fast. Clock frequency is currently limited to 100ms.");
            return false;
        }

        info(sender, "Clock will tick every " + interval + " milliseconds for " + onInterval + " milliseconds.");

        if (onInterval>0) {
            thread = new TickThread();
        } else {
            thread = new ZeroPulseTickThread();
        }

        return true;
    }

    @Override
    public void circuitDestroyed() {
        if (running) {
            stopClock();
        }
    }

    private void startClock() {
        if (thread==null) { running = false; return; }

        thread.start();
        running = true;
    }

    private void stopClock() {
        if (thread==null) { running = false; return; }

        thread.interrupt();
        if (onInterval>0)
            thread = new TickThread();
        else thread = new ZeroPulseTickThread();
        running = false;
    }

    private void setFreq(long freq, double pulseWidth) {
        this.interval = freq;
        this.onInterval = Math.round(freq*pulseWidth);
        this.offInterval = freq - onInterval;
    }

    class TickThread extends Thread {
        private Runnable updateOutputsTask;

        private boolean state = false;

        @Override
        public void run() {
            updateOutputsTask = new Runnable() {
                @Override
                public void run() {
                    if (state) { // turn on any output whose input is on
                        BitSet7 out = (BitSet7)inputBits.clone();
                        out.and(onBits);
                        sendBitSet(0, outputs.length, out);
                    } else { // just clear everything
                        sendBitSet(0, outputs.length, offBits);
                    }
                }
            };

            state = true;
            if (hasDebuggers()) debug("Starting clock.");
            try {
                while(true) {
                    redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, updateOutputsTask);
                    if (state) {
                        if (onInterval>0) Thread.sleep(onInterval);
                    } else if (offInterval>0) Thread.sleep(offInterval);
                    state = !state;
                }
            } catch (InterruptedException ie) {
                if (hasDebuggers()) debug("Stopping clock.");
                state = false;
                redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, updateOutputsTask);
            }
        }
    }

    class ZeroPulseTickThread extends Thread {
        private Runnable updateOutputsTask;

        @Override
        public void run() {
            updateOutputsTask = new Runnable() {
                @Override
                public void run() {
                    BitSet7 out = (BitSet7)inputBits.clone();
                    out.and(onBits);
                    sendBitSet(0, outputs.length, out);
                    sendBitSet(0, outputs.length, offBits);
                }
            };

            if (hasDebuggers()) debug("Starting clock.");
            try {
                while(true) {
                    redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, updateOutputsTask);
                    Thread.sleep(interval);
                }
            } catch (InterruptedException ie) {
                if (hasDebuggers()) debug("Stopping clock.");
            }

        }
    }
}

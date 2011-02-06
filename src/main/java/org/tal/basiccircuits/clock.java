package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class clock extends Circuit {
    private long freq;
    private long onInterval, offInterval;

    private boolean running = false;
    private BitSet7 onBits, offBits;
    private TickThread thread = new TickThread();

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        // if not running any change from low to high will start it.

        if (newLevel) { // change from low to high
            if (!running) startClock();
        } else {
            if (running && inputBits.isEmpty()) stopClock();
        }
    }

    @Override
    public boolean init(Player player, String[] args) {
        // one argument for duration. number of inputs should match number of outputs.
        double pulseWidth = 0.5;
        if (args.length==0) setFreq(1000, pulseWidth); // 1 sec default, 50% pulse width
        else {            
            if (args.length>1) {
                try {
                    pulseWidth = Double.parseDouble(args[1]);
                } catch (NumberFormatException ne) {
                    error(player, "Bad floating-point number: " + pulseWidth);
                }
            }
            setFreq(Math.round(UnitParser.parse(args[0])), pulseWidth);
        }

        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same amount of inputs and outputs.");
            return false;
        }

        onBits = new BitSet7(inputs.length);
        offBits = new BitSet7(inputs.length);

        onBits.set(0, inputs.length);
        offBits.clear();
        if (freq<200) {
            error(player, "Clock is set to tick too fast. Clock frequency is currently limited to 200ms.");
            return false;
        }
        info(player, "Clock will tick every " + freq + " milliseconds for " + onInterval + " milliseconds.");
        info(player, ChatColor.LIGHT_PURPLE + "WARNING: The clock circuit is still very unstable. Use at your own risk, and expect server crashes.");
        return true;
    }

    @Override
    public void circuitDestroyed() {
        if (running) {
            stopClock();
        }
    }

    private void startClock() {
        thread.start();
        running = true;
    }

    private void stopClock() {
        thread.interrupt();
        thread = new TickThread();
        running = false;
    }

    private void setFreq(long freq, double pulseWidth) {
        this.freq = freq;
        this.onInterval = Math.round(freq*pulseWidth);
        this.offInterval = freq - onInterval;
    }

    @Override
    public void loadState(Map<String, String> state) {
        inputBits = Circuit.loadBitSet(state, "inputBits");

        if (inputBits.isEmpty() && running) stopClock();
        else if (!inputBits.isEmpty() && !running) startClock();
    }

    @Override
    public Map<String, String> saveState() {
        return Circuit.storeBitSet(new HashMap<String, String>(), "inputBits", inputBits, inputs.length);
    }

    class TickThread extends Thread {
        boolean state = false;
        @Override
        public void run() {
            state = true;
            if (hasDebuggers()) debug("Starting clock.");
            try {
                while(true) {
                    update();
                    if (state) Thread.sleep(onInterval);
                    else Thread.sleep(offInterval);
                    state = !state;
                }
            } catch (InterruptedException ie) {
                if (hasDebuggers()) debug("Stopping clock.");
                state = false;
                update();
            }
        }

        private void update() {
            if (state) { // turn on any output whose input is on
                BitSet7 out = (BitSet7)inputBits.clone();
                out.and(onBits);
                synchronized(this) {
                    sendBitSet(0, outputs.length, out);
                }
            } else { // just clear everything
                synchronized(this) {
                    sendBitSet(0, outputs.length, offBits);
                }
            }
        }

    }
}

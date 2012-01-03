package org.tal.basiccircuits;

import java.io.File;
import java.util.logging.Level;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.CircuitLibrary;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends CircuitLibrary {
    @Override
    public Class[] getCircuitClasses() {
        return new Class[] { adder.class, and.class, clock.class, counter.class, demultiplexer.class, divider.class, flipflop.class,
                multiplexer.class, multiplier.class, or.class, pisoregister.class, print.class, random.class, receiver.class,
                shiftregister.class, transmitter.class, xor.class, decoder.class, encoder.class, pixel.class, pulse.class, not.class,
                synth.class, srnor.class, terminal.class, router.class, ringcounter.class, iptransmitter.class, ipreceiver.class,
                comparator.class, delay.class, repeater.class, nand.class, nor.class, xnor.class, segdriver.class, dregister.class, 
                sram.class, bintobcd.class, display.class, burst.class };
    }

    @Override
    public void onRedstoneChipsEnable(RedstoneChips rc) {
        // add new pref keys.
        rc.getPrefs().registerCircuitPreference(iptransmitter.class, "ports", "25600..25699");

        if (rc.getDataFolder()==null || !rc.getDataFolder().exists()) return;
        
        // set sram data folder.
        try {
            boolean s = sram.setupDataFolder(rc.getDataFolder());
            if (s) logger.log(Level.INFO, "[BasicCircuits] Created new sram folder: " + sram.dataFolder.getAbsolutePath());
        } catch (Exception e) {
            logger.log(Level.INFO, "[BasicCircuits] " + e.getMessage());
        }

        // move any sram files in rc data folder.
        if (rc.getDataFolder().listFiles()!=null) {
            for (File f : rc.getDataFolder().listFiles()) {
                if (f.isFile() && f.getName().startsWith("sram-") &&
                        f.getName().endsWith(".data")) {
                    f.renameTo(new File(sram.dataFolder, f.getName()));
                }
            }
        }
    }
}
